resource "tls_private_key" "jwt" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "aws_secretsmanager_secret" "jwt_keys" {
  name = "${var.project_name}/jwt/keypair"
}

resource "aws_secretsmanager_secret_version" "jwt_keys" {
  secret_id = aws_secretsmanager_secret.jwt_keys.id
  secret_string = jsonencode({
    private_key = tls_private_key.jwt.private_key_pem_pkcs8
    public_key  = tls_private_key.jwt.public_key_pem
  })
}

resource "aws_secretsmanager_secret" "stripe" {
  name = "${var.project_name}/stripe/keys"
}

resource "aws_secretsmanager_secret_version" "stripe" {
  secret_id = aws_secretsmanager_secret.stripe.id
  secret_string = jsonencode({
    secret_key     = var.stripe_secret_key
    webhook_secret = var.stripe_webhook_secret
  })
}

data "aws_iam_policy_document" "eso_assume_role" {
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
      values   = ["system:serviceaccount:${var.eso_namespace}:${var.eso_service_account}"]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(var.oidc_provider_url, "https://", "")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "eso" {
  name               = "${var.project_name}-external-secrets"
  assume_role_policy = data.aws_iam_policy_document.eso_assume_role.json
}

data "aws_iam_policy_document" "eso_secrets_read" {
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
    ]
    resources = [
      var.rds_secret_arn,
      aws_secretsmanager_secret.jwt_keys.arn,
      aws_secretsmanager_secret.stripe.arn,
    ]
  }
}

resource "aws_iam_policy" "eso_secrets_read" {
  name   = "${var.project_name}-eso-secrets-read"
  policy = data.aws_iam_policy_document.eso_secrets_read.json
}

resource "aws_iam_role_policy_attachment" "eso_secrets_read" {
  role       = aws_iam_role.eso.name
  policy_arn = aws_iam_policy.eso_secrets_read.arn
}
