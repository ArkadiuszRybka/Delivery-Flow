variable "aws_region" {
  description = "AWS region for all DeliveryFlow infrastructure"
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Project name, used as a prefix for resource naming"
  type        = string
  default     = "deliveryflow"
}

variable "stripe_secret_key" {
  description = "Stripe test-mode secret key, passed via TF_VAR_stripe_secret_key - never hardcoded"
  type        = string
  sensitive   = true
}

variable "stripe_webhook_secret" {
  description = "Stripe webhook signing secret, updated once the real endpoint exists (Sekcja H)"
  type        = string
  sensitive   = true
  default     = "whsec_placeholder"
}
