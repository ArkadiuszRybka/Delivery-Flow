output "repository_urls" {
  value = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

output "github_actions_role_arn" {
  value = aws_iam_role.github_actions.arn
}
