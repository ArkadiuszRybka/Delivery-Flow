variable "project_name" {
  type = string
}

variable "services" {
  type    = list(string)
  default = ["api-gateway", "order-service", "tracking-service", "notification-service"]
}

variable "github_repo" {
  type        = string
  description = "GitHub repo allowed to assume the CI role, in owner/repo form"
  default     = "ArkadiuszRybka/Delivery-Flow"
}
