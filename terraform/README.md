# Sentinel AI - Cloud Run Deployment

This directory contains Terraform configuration for deploying Sentinel AI to Google Cloud Run.

## Prerequisites

1. **Google Cloud Project**
   - Create a GCP project
   - Enable billing
   - Enable required APIs

2. **Tools**
   - Google Cloud SDK (`gcloud`)
   - Terraform >= 1.0
   - Docker

3. **Authentication**
   ```bash
   gcloud auth login
   gcloud auth application-default login
   ```

## Quick Start

### 1. Configure Variables

Edit `terraform.tfvars`:
```hcl
project_id = "qwiklabs-asl-03-085d8ab3ed6e"
region     = "us-central1"
```

### 2. Initialize Terraform

```bash
cd terraform
terraform init
```

### 3. Review Plan

```bash
terraform plan
```

### 4. Deploy

```bash
terraform apply
```

### 5. Get Service URL

```bash
terraform output service_url
```

## What Gets Deployed

- **Artifact Registry**: Docker image repository
- **Cloud Run Service**: Sentinel AI API service
- **Service Account**: With appropriate permissions
- **IAM Bindings**: Public access or authenticated access
- **Secret Manager**: For API keys and sensitive config

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Google Cloud                        │
│                                                         │
│  ┌────────────────────────────────────────────────┐   │
│  │         Artifact Registry                       │   │
│  │  sentinel-ai-images                             │   │
│  │    └─ sentinel-api:latest                       │   │
│  └────────────────────────────────────────────────┘   │
│                           │                             │
│                           ▼                             │
│  ┌────────────────────────────────────────────────┐   │
│  │         Cloud Run Service                       │   │
│  │  sentinel-api                                   │   │
│  │    • Min instances: 0                           │   │
│  │    • Max instances: 10                          │   │
│  │    • CPU: 2                                     │   │
│  │    • Memory: 2Gi                                │   │
│  │    • Timeout: 300s                              │   │
│  │    • Concurrency: 80                            │   │
│  └────────────────────────────────────────────────┘   │
│                           │                             │
│                           ▼                             │
│  ┌────────────────────────────────────────────────┐   │
│  │         Secret Manager                          │   │
│  │    • GEMINI_API_KEY                             │   │
│  │    • Other secrets                              │   │
│  └────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Configuration Options

### Environment Variables

Set in `terraform.tfvars`:

```hcl
env_vars = {
  "GEMINI_MODEL"     = "gemini-2.5-flash"
  "LOG_LEVEL"        = "INFO"
  "POLICY_VERSION"   = "action-policy-2026-03-16"
}
```

### Secrets

Managed via Secret Manager:

```hcl
secrets = {
  "GEMINI_API_KEY" = "projects/PROJECT_ID/secrets/gemini-api-key/versions/latest"
}
```

### Scaling

```hcl
min_instances = 0    # Scale to zero when idle
max_instances = 10   # Max concurrent instances
```

### Resources

```hcl
cpu_limit    = "2"     # CPU cores
memory_limit = "2Gi"   # Memory
timeout      = 300     # Request timeout in seconds
```

## Build and Deploy Script

Use the provided script:

```bash
./scripts/deploy.sh
```

This will:
1. Build the Docker image
2. Push to Artifact Registry
3. Deploy to Cloud Run
4. Output the service URL

## Manual Deployment

### 1. Build Docker Image

```bash
docker build -t gcr.io/PROJECT_ID/sentinel-api:latest .
```

### 2. Push to Artifact Registry

```bash
docker push gcr.io/PROJECT_ID/sentinel-api:latest
```

### 3. Deploy to Cloud Run

```bash
gcloud run deploy sentinel-api \
  --image gcr.io/PROJECT_ID/sentinel-api:latest \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated
```

## Accessing the Service

### Public Access (Development)

```bash
curl $(terraform output -raw service_url)/health
```

### Authenticated Access (Production)

```bash
TOKEN=$(gcloud auth print-identity-token)
curl -H "Authorization: Bearer $TOKEN" $(terraform output -raw service_url)/health
```

## Monitoring

### Logs

```bash
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=sentinel-api" --limit 50
```

### Metrics

View in Cloud Console:
- Cloud Run > sentinel-api > Metrics

### Alerts

Terraform creates basic alerts for:
- High error rate
- High latency
- Resource exhaustion

## Cost Estimation

Approximate monthly costs (us-central1):

| Resource | Usage | Cost |
|----------|-------|------|
| Cloud Run | 100K requests, 1s avg | ~$5 |
| Artifact Registry | 1 GB storage | ~$0.10 |
| Secret Manager | 2 secrets | ~$0.12 |
| **Total** | | **~$5.22/month** |

*Costs scale with usage. Free tier available.*

## Cleanup

Remove all resources:

```bash
terraform destroy
```

## Troubleshooting

### Image Pull Errors

Ensure Service Account has `artifactregistry.reader` role:
```bash
gcloud projects add-iam-policy-binding PROJECT_ID \
  --member=serviceAccount:SERVICE_ACCOUNT \
  --role=roles/artifactregistry.reader
```

### Out of Memory

Increase memory in `terraform.tfvars`:
```hcl
memory_limit = "4Gi"
```

### Cold Start Issues

Set minimum instances:
```hcl
min_instances = 1
```

## Security Best Practices

1. **Use Secret Manager** for API keys
2. **Enable VPC Connector** for private networking
3. **Restrict IAM** to specific users/services
4. **Enable Cloud Armor** for DDoS protection
5. **Use Cloud CDN** for caching
6. **Implement Cloud Audit Logs**

## CI/CD Integration

### GitHub Actions

See `.github/workflows/deploy.yml`

### Cloud Build

```bash
gcloud builds submit --config cloudbuild.yaml
```

## Support

For issues or questions:
- Check logs: `terraform output logs_url`
- Review metrics: `terraform output metrics_url`
- GCP Support: https://cloud.google.com/support

---

**Version**: 1.0.0  
**Last Updated**: March 16, 2026
