output "eso_role_arn" {
  value = aws_iam_role.eso.arn
}

output "jwt_secret_arn" {
  value = aws_secretsmanager_secret.jwt_keys.arn
}

output "stripe_secret_arn" {
  value = aws_secretsmanager_secret.stripe.arn
}
