# Sentinel AI - Google Cloud Deployment Guide

Complete guide for deploying Sentinel AI to Google Cloud Run using Terraform.

## 🚀 Quick Start (5 minutes)

### Prerequisites

```bash
# Install required tools
brew install google-cloud-sdk terraform docker

# Login to Google Cloud
gcloud auth login
gcloud auth application-default login
```

### Step-by-Step Deployment

1. **Create GCP Project**
   ```bash
   # Create project (or use existing)
   gcloud projects create sentinel-ai-prod --name="Sentinel AI Production"
   
   # Set project
   export PROJECT_ID="sentinel-ai-prod"
   gcloud config set project $PROJECT_ID
   
   # Enable billing (required)
   # Visit: https://console.cloud.google.com/billing
   ```

2. **Configure Terraform**
   ```bash
   cd terraform
   cp terraform.tfvars.example terraform.tfvars
   
   # Edit terraform.tfvars with your project ID
   vim terraform.tfvars
   ```

3. **Deploy**
   ```bash
   cd ..
   ./scripts/deploy.sh
   ```

4. **Test**
   ```bash
   # Get service URL
   cd terraform
   SERVICE_URL=$(terraform output -raw service_url)
   
   # Test the service
   curl $SERVICE_URL/health
   ```

## 📋 Detailed Setup

### 1. Google Cloud Project Setup

#### Create Project
```bash
export PROJECT_ID="your-project-id"
gcloud projects create $PROJECT_ID
gcloud config set project $PROJECT_ID
```

#### Enable Billing
```bash
# List billing accounts
gcloud billing accounts list

# Link billing account
gcloud billing projects link $PROJECT_ID \
  --billing-account=BILLING_ACCOUNT_ID
```

#### Enable Required APIs
```bash
gcloud services enable run.googleapis.com
gcloud services enable artifactregistry.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

### 2. Set Up Secrets (Optional)

If you need to store API keys:

```bash
# Create secret for Gemini API key
echo -n "your-gemini-api-key" | gcloud secrets create gemini-api-key \
  --data-file=- \
  --replication-policy="automatic"

# Grant access to Cloud Run service account (will be created by Terraform)
gcloud secrets add-iam-policy-binding gemini-api-key \
  --member="serviceAccount:sentinel-api-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

Then update `terraform.tfvars`:
```hcl
secrets = {
  "GEMINI_API_KEY" = "projects/${PROJECT_ID}/secrets/gemini-api-key/versions/latest"
}
```

### 3. Configure Terraform

#### Create terraform.tfvars
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

#### Minimal Configuration
```hcl
project_id = "your-gcp-project-id"
region     = "us-central1"
```

#### Full Configuration (Optional)
```hcl
project_id = "your-gcp-project-id"
region     = "us-central1"

# Resource limits
cpu_limit    = "2"
memory_limit = "2Gi"

# Scaling
min_instances = 1  # Keep 1 instance warm
max_instances = 10

# Access (dev only - set to false for production)
allow_unauthenticated = true

# Environment variables
env_vars = {
  "GEMINI_MODEL"   = "gemini-2.5-flash"
  "LOG_LEVEL"      = "INFO"
  "POLICY_VERSION" = "action-policy-2026-03-16"
}

# Secrets (if configured)
secrets = {
  "GEMINI_API_KEY" = "projects/your-project-id/secrets/gemini-api-key/versions/latest"
}
```

### 4. Deploy Using Script

The deploy script handles everything:

```bash
cd /path/to/sentinel-ai
./scripts/deploy.sh
```

This will:
1. Build the Java application with Maven
2. Build the Docker image
3. Push to Artifact Registry
4. Deploy to Cloud Run via Terraform

### 5. Manual Deployment (Alternative)

#### Initialize Terraform
```bash
cd terraform
terraform init
```

#### Plan
```bash
terraform plan
```

#### Apply
```bash
terraform apply
```

#### Get Outputs
```bash
terraform output service_url
terraform output image_url
```

## 🔧 Configuration Options

### Environment Variables

Set in `terraform.tfvars`:

```hcl
env_vars = {
  "GEMINI_MODEL"     = "gemini-2.5-flash"
  "LOG_LEVEL"        = "INFO"
  "POLICY_VERSION"   = "action-policy-2026-03-16"
  "JAVA_OPTS"        = "-Xmx1g -XX:+UseG1GC"
}
```

### Resource Allocation

```hcl
# CPU and Memory
cpu_limit    = "2"      # 1, 2, 4, 6, 8
memory_limit = "2Gi"    # 128Mi, 256Mi, 512Mi, 1Gi, 2Gi, 4Gi, 8Gi

# Timeout
timeout = 300  # seconds (max 3600)

# Concurrency
concurrency = 80  # concurrent requests per instance
```

### Scaling

```hcl
# Auto-scaling
min_instances = 0   # Scale to zero when idle (saves cost)
max_instances = 10  # Maximum instances

# Or keep warm instances
min_instances = 1   # Always keep 1 instance ready
max_instances = 20  # Higher max for more traffic
```

### Access Control

```hcl
# Public access (development)
allow_unauthenticated = true

# Authenticated access (production)
allow_unauthenticated = false
```

## 🧪 Testing Deployment

### Health Check
```bash
curl https://sentinel-api-xxxxx-uc.a.run.app/health
```

### Test Orchestrator (if public)
```bash
SERVICE_URL=$(cd terraform && terraform output -raw service_url)

# Send demo request
curl -X POST $SERVICE_URL/process \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "tx_test_001",
    "amount": 1500.00,
    "merchant": "Electronics Store"
  }'
```

### Test with Authentication (if private)
```bash
TOKEN=$(gcloud auth print-identity-token)
curl -H "Authorization: Bearer $TOKEN" $SERVICE_URL/health
```

## 📊 Monitoring & Logs

### View Logs
```bash
# Real-time logs
gcloud logging tail "resource.type=cloud_run_revision AND resource.labels.service_name=sentinel-api"

# Recent logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=sentinel-api" --limit 50
```

### View Metrics
```bash
# Open metrics in browser
cd terraform
open $(terraform output -raw metrics_url)
```

### Set Up Alerts
```bash
# CPU utilization alert
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="Sentinel API - High CPU" \
  --condition-display-name="CPU > 80%" \
  --condition-threshold-value=0.8 \
  --condition-threshold-duration=60s
```

## 🔄 Updates & Redeployment

### Update Code
```bash
# Make code changes
# Then redeploy
./scripts/deploy.sh
```

### Update Configuration Only
```bash
cd terraform
terraform apply
```

### Rollback
```bash
# List revisions
gcloud run revisions list --service=sentinel-api --region=us-central1

# Roll back to previous revision
gcloud run services update-traffic sentinel-api \
  --region=us-central1 \
  --to-revisions=REVISION_NAME=100
```

## 💰 Cost Optimization

### Estimated Monthly Costs

| Usage | Cost (us-central1) |
|-------|-------------------|
| 10K requests/month, 1s avg | ~$1 |
| 100K requests/month, 1s avg | ~$5 |
| 1M requests/month, 1s avg | ~$50 |

### Cost Optimization Tips

1. **Scale to Zero**
   ```hcl
   min_instances = 0  # No cost when idle
   ```

2. **Reduce Memory**
   ```hcl
   memory_limit = "1Gi"  # Lower memory = lower cost
   ```

3. **Use Smaller Region**
   ```hcl
   region = "us-central1"  # Cheaper than multi-region
   ```

4. **Set Request Timeout**
   ```hcl
   timeout = 60  # Don't pay for long-running requests
   ```

5. **Monitor Usage**
   ```bash
   # Check current month costs
   gcloud billing accounts get-default-project-billing-info
   ```

## 🔒 Security Best Practices

### 1. Disable Public Access (Production)
```hcl
allow_unauthenticated = false
```

### 2. Use Secret Manager
```hcl
secrets = {
  "GEMINI_API_KEY" = "projects/PROJECT/secrets/gemini-api-key/versions/latest"
}
```

### 3. Enable VPC Connector (Optional)
```hcl
# Add to cloud_run.tf
vpc_access {
  connector = google_vpc_access_connector.connector.id
  egress    = "ALL_TRAFFIC"
}
```

### 4. Set Up IAM Roles
```bash
# Grant specific users access
gcloud run services add-iam-policy-binding sentinel-api \
  --region=us-central1 \
  --member=user:user@example.com \
  --role=roles/run.invoker
```

### 5. Enable Cloud Armor (DDoS Protection)
```bash
gcloud compute security-policies create sentinel-protection
gcloud compute security-policies rules create 1000 \
  --security-policy=sentinel-protection \
  --expression="origin.region_code == 'US'" \
  --action=allow
```

## 🚨 Troubleshooting

### Issue: Terraform Apply Fails

**Solution**: Check API enablement
```bash
gcloud services list --enabled
```

### Issue: Image Push Fails

**Solution**: Configure Docker auth
```bash
gcloud auth configure-docker us-central1-docker.pkg.dev
```

### Issue: Service Won't Start

**Solution**: Check logs
```bash
gcloud logging read "resource.type=cloud_run_revision" --limit 50
```

### Issue: Out of Memory

**Solution**: Increase memory
```hcl
memory_limit = "4Gi"
```

### Issue: Slow Cold Starts

**Solution**: Keep instances warm
```hcl
min_instances = 1
```

## 📚 Additional Resources

- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Terraform Google Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [ADK Documentation](https://github.com/google/adk)
- [Sentinel AI Architecture](../doc/dashboard.md)

## 🆘 Support

For deployment issues:
1. Check logs: `gcloud logging read ...`
2. Check service status: `gcloud run services describe sentinel-api`
3. Review Terraform state: `terraform show`
4. Contact GCP Support: https://cloud.google.com/support

---

**Last Updated**: March 16, 2026  
**Terraform Version**: >= 1.0  
**Google Provider Version**: ~> 5.0
