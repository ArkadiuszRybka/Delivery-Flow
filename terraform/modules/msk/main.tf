resource "aws_security_group" "msk" {
  name        = "${var.project_name}-msk"
  description = "Allow Kafka (IAM auth) access from within the VPC"
  vpc_id      = var.vpc_id

  ingress {
    description = "Kafka IAM-auth from VPC"
    from_port   = 9098
    to_port     = 9098
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-msk-sg"
  }
}

resource "aws_msk_serverless_cluster" "main" {
  cluster_name = "${var.project_name}-msk"

  vpc_config {
    subnet_ids         = var.private_subnet_ids
    security_group_ids = [aws_security_group.msk.id]
  }

  client_authentication {
    sasl {
      iam {
        enabled = true
      }
    }
  }

  tags = {
    Name = "${var.project_name}-msk"
  }
}

data "aws_iam_policy_document" "kafka_client_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [var.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(var.oidc_provider_url, "https://", "")}:sub"
      values   = ["system:serviceaccount:${var.kafka_client_namespace}:${var.kafka_client_service_account}"]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(var.oidc_provider_url, "https://", "")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "kafka_client" {
  name               = "${var.project_name}-kafka-client"
  assume_role_policy = data.aws_iam_policy_document.kafka_client_assume_role.json
}

data "aws_iam_policy_document" "kafka_client_permissions" {
  statement {
    effect = "Allow"
    actions = [
      "kafka-cluster:Connect",
      "kafka-cluster:DescribeCluster",
    ]
    resources = [aws_msk_serverless_cluster.main.arn]
  }

  statement {
    effect = "Allow"
    actions = [
      "kafka-cluster:*Topic*",
      "kafka-cluster:ReadData",
      "kafka-cluster:WriteData",
    ]
    resources = ["${replace(aws_msk_serverless_cluster.main.arn, ":cluster/", ":topic/")}/order.events"]
  }

  statement {
    effect = "Allow"
    actions = [
      "kafka-cluster:AlterGroup",
      "kafka-cluster:DescribeGroup",
    ]
    resources = ["${replace(aws_msk_serverless_cluster.main.arn, ":cluster/", ":group/")}/notification-service-group"]
  }
}

resource "aws_iam_policy" "kafka_client" {
  name   = "${var.project_name}-kafka-client"
  policy = data.aws_iam_policy_document.kafka_client_permissions.json
}

resource "aws_iam_role_policy_attachment" "kafka_client" {
  role       = aws_iam_role.kafka_client.name
  policy_arn = aws_iam_policy.kafka_client.arn
}
