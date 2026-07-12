output "bootstrap_brokers_sasl_iam" {
  value = aws_msk_serverless_cluster.main.bootstrap_brokers_sasl_iam
}

output "kafka_client_role_arn" {
  value = aws_iam_role.kafka_client.arn
}
