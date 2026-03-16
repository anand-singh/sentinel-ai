variable "project_id" {
}
  }
    "managed-by"  = "terraform"
    "environment" = "production"
    "app"         = "sentinel-ai"
  default = {
  type        = map(string)
  description = "Labels to apply to resources"
variable "labels" {

}
  default     = {}
  type        = map(string)
  description = "Secret environment variables (from Secret Manager)"
variable "secrets" {

}
  }
    "POLICY_VERSION" = "action-policy-2026-03-16"
    "LOG_LEVEL"      = "INFO"
    "GEMINI_MODEL"   = "gemini-2.5-flash"
  default = {
  type        = map(string)
  description = "Environment variables for the service"
variable "env_vars" {

}
  default     = false
  type        = bool
  description = "Allow unauthenticated access to the service"
variable "allow_unauthenticated" {

}
  default     = 80
  type        = number
  description = "Maximum concurrent requests per instance"
variable "concurrency" {

}
  default     = 300
  type        = number
  description = "Request timeout in seconds"
variable "timeout" {

}
  default     = 10
  type        = number
  description = "Maximum number of instances"
variable "max_instances" {

}
  default     = 0
  type        = number
  description = "Minimum number of instances"
variable "min_instances" {

}
  default     = "2Gi"
  type        = string
  description = "Memory limit for Cloud Run service"
variable "memory_limit" {

}
  default     = "2"
  type        = string
  description = "CPU limit for Cloud Run service"
variable "cpu_limit" {

}
  default     = "latest"
  type        = string
  description = "Docker image tag"
variable "image_tag" {

}
  default     = "sentinel-api"
  type        = string
  description = "Docker image name"
variable "image_name" {

}
  default     = "sentinel-api"
  type        = string
  description = "Name of the Cloud Run service"
variable "service_name" {

}
  default     = "us-central1"
  type        = string
  description = "GCP region for resources"
variable "region" {

}
  type        = string
  description = "GCP Project ID"
