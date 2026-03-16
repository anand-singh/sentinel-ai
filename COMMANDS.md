# Quick reference commands for Sentinel AI deployment

## Local Development
```bash
# Run orchestrator locally
cd api
mvn exec:java@orchestrator

# Run individual agents
mvn exec:java@pattern-analyzer
mvn exec:java@behavioral-risk
mvn exec:java@action-executor

# Build
mvn clean package
```

## Docker Local
```bash
# Build and run locally
docker build -t sentinel-api:local -f api/Dockerfile api/
docker run -p 8080:8080 sentinel-api:local

# Development mode with hot reload
docker build -t sentinel-api:dev -f api/Dockerfile.dev api/
docker run -p 8080:8080 -v $(pwd)/api/src:/app/src sentinel-api:dev
```

## Google Cloud Deployment

### First Time Setup
```bash
# 1. Set project
export PROJECT_ID="your-gcp-project-id"
gcloud config set project $PROJECT_ID

# 2. Enable APIs
gcloud services enable run.googleapis.com artifactregistry.googleapis.com

# 3. Configure Terraform
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your project_id

# 4. Deploy
cd ..
./scripts/deploy.sh
```

### Updates
```bash
# Quick redeploy
./scripts/deploy.sh

# Or manual
cd api && mvn clean package && cd ..
cd terraform
terraform apply
```

### Useful Commands
```bash
# Get service URL
cd terraform && terraform output service_url

# View logs
gcloud logging tail "resource.type=cloud_run_revision AND resource.labels.service_name=sentinel-api"

# Get auth token
gcloud auth print-identity-token

# Test endpoint
curl -H "Authorization: Bearer $(gcloud auth print-identity-token)" \
  $(cd terraform && terraform output -raw service_url)/health
```

## Monitoring
```bash
# View metrics
cd terraform && open $(terraform output -raw metrics_url)

# View logs in console
cd terraform && open $(terraform output -raw logs_url)

# Stream logs
gcloud logging read "resource.type=cloud_run_revision" --limit=50 --format=json
```

## Cleanup
```bash
# Remove all cloud resources
cd terraform
terraform destroy
```

## Cost Check
```bash
# Current month usage
gcloud billing accounts list
gcloud billing projects describe $PROJECT_ID

# View in console
open https://console.cloud.google.com/billing
```
