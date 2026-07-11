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
