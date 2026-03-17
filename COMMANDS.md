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
# Delete Cloud Run service
gcloud run services delete sentinel-api --region europe-west1 --quiet

# Delete container images
gcloud artifacts docker images delete \
  europe-west1-docker.pkg.dev/$PROJECT_ID/sentinel-ai/api:latest \
  --delete-tags --quiet

# Delete Artifact Registry repository
gcloud artifacts repositories delete sentinel-ai \
  --location=europe-west1 --quiet
```

---

## Development Tips

### Local Testing with Mock Data

```bash
# Test individual agent without GCP credentials (uses mock tools)
cd api
mvn compile exec:java@pattern-analyzer

# Example input:
# Transaction: amount=1500, merchant=electronics, country=NG
```

### Debugging

```bash
# Enable debug logging
mvn compile exec:java@orchestrator -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG

# View ADK traces in Dev UI
mvn compile exec:java@dev-ui
# Opens at http://localhost:8080 with agent testing interface
```

### Hot Reload Development

```bash
# Watch for changes and recompile
mvn compile -Dmaven.compiler.useIncrementalCompilation=false

# In another terminal, run agent
mvn compile exec:java@pattern-analyzer
```

---

## Troubleshooting

### Common Issues

**"Cannot find symbol: class McpToolset"**
- Solution: Using old ADK API, update to 0.9.0 in pom.xml

**"GOOGLE_APPLICATION_CREDENTIALS not set"**
- Solution: Export credentials path: `export GOOGLE_APPLICATION_CREDENTIALS=/path/to/sa.json`

**"Permission denied: Vertex AI"**
- Solution: Grant service account `roles/aiplatform.user` role

**"Cloud Run service unreachable"**
- Solution: Check service status: `gcloud run services describe sentinel-api --region europe-west1`

### Useful Debugging Commands

```bash
# Check Java version (must be 17+)
java -version

# Verify Maven version (must be 3.8+)
mvn -version

# List all dependencies
cd api && mvn dependency:tree

# Force dependency refresh
cd api && mvn clean install -U

# Check environment variables
env | grep GOOGLE

# Verify service account permissions
gcloud projects get-iam-policy $PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:sentinel-api@*"

# Test Vertex AI access
gcloud ai models list --region=us-central1
```

---

## Agent Execution IDs (pom.xml)

| Exec ID | Main Class | Agent | Description |
|---------|------------|-------|-------------|
| `orchestrator` | `SentinelOrchestrator` | Orchestrator | Full 5-agent pipeline |
| `pattern-analyzer` | `TransactionPatternAnalyzer` | Agent #1 | Global pattern detection |
| `behavioral-risk` | `BehavioralRiskDetector` | Agent #2 | Customer behavior analysis |
| `evidence-builder` | `EvidenceBuilderAgent` | Agent #3 | Evidence compilation |
| `action-executor` | `ActionExecutor` | Agent #5 | Action execution |
| `dev-ui` | `SentinelDevServer` | Dev UI | Browser-based testing |

---

## Quick Reference

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GOOGLE_CLOUD_PROJECT` | Yes | GCP project ID |
| `GOOGLE_APPLICATION_CREDENTIALS` | Yes | Path to service account JSON |
| `GEMINI_MODEL` | No | Model name (default: gemini-2.5-flash) |
| `MCP_TOOLBOX_URL` | No | MCP server URL (optional) |

### Project Stats

- **Total Agents**: 6 (orchestrator + 5 pipeline agents)
- **Total FunctionTools**: 33 (across 6 tool directories)
- **Google ADK Version**: 0.9.0
- **Java Version**: 17
- **Maven Version**: 3.8+

### Key File Locations

- Agents: `api/src/main/java/com/ing/sentinel/agents/`
- Tools: `api/src/main/java/com/ing/sentinel/tools/`
- Config: `api/pom.xml`
- Dockerfile: `api/Dockerfile`

### Important URLs

- ADK Docs: https://google.github.io/adk-docs/#java
- Vertex AI Console: https://console.cloud.google.com/vertex-ai
- Cloud Run Console: https://console.cloud.google.com/run

