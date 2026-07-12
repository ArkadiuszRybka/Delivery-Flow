variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "vpc_cidr" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "oidc_provider_arn" {
  type = string
}

variable "oidc_provider_url" {
  type = string
}

variable "kafka_client_namespace" {
  type    = string
  default = "default"
}

variable "kafka_client_service_account" {
  type    = string
  default = "kafka-client"
}
