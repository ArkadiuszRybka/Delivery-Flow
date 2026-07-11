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
