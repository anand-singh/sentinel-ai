output "service_url" {
  description = "URL of the Cloud Run service"
  value       = google_cloud_run_v2_service.sentinel_api.uri
}

output "service_name" {
  description = "Name of the Cloud Run service"
  value       = google_cloud_run_v2_service.sentinel_api.name
}

output "service_account_email" {
  description = "Email of the service account"
  value       = google_service_account.sentinel_api.email
}

output "artifact_registry_url" {
  description = "URL of the Artifact Registry repository"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.sentinel_images.repository_id}"
}

output "image_url" {
  description = "Full image URL"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.sentinel_images.repository_id}/${var.image_name}:${var.image_tag}"
}

output "logs_url" {
  description = "URL to view logs in Cloud Console"
  value       = "https://console.cloud.google.com/run/detail/${var.region}/${var.service_name}/logs?project=${var.project_id}"
}

output "metrics_url" {
  description = "URL to view metrics in Cloud Console"
  value       = "https://console.cloud.google.com/run/detail/${var.region}/${var.service_name}/metrics?project=${var.project_id}"
}

output "deployment_instructions" {
  description = "Instructions for deploying the Docker image"
  value = <<-EOT
    To deploy a new version:

    1. Build and push the image:
       docker build -t ${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.sentinel_images.repository_id}/${var.image_name}:${var.image_tag} ../api
       docker push ${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.sentinel_images.repository_id}/${var.image_name}:${var.image_tag}

    2. Redeploy the service:
       gcloud run services update ${var.service_name} --region ${var.region}

    Or use the deploy script:
       ./scripts/deploy.sh
  EOT
}
