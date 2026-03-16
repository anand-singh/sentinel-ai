# Sentinel AI - API

Java/Maven API service using Google ADK for multi-agent fraud defense.

---

## 🚀 Getting Started (Development)

### Prerequisites

| Requirement        | Version  | Notes                                    |
|--------------------|----------|------------------------------------------|
| **Java JDK**       | 17+      | OpenJDK or Oracle JDK                    |
| **Maven**          | 3.8+     | Build & dependency management            |
| **Docker**         | 20+      | (Optional) For containerized development |
| **Google Cloud SDK** | Latest | For Vertex AI / ADK integrations         |

### Environment Variables

Create a `.env` file or export these variables:

```bash
# MCP Toolbox URL (for Cloud SQL integration)
export MCP_TOOLBOX_URL="http://127.0.0.1:5000/mcp/"

# Google Cloud / Vertex AI (required for Gemini models)
export GOOGLE_CLOUD_PROJECT="your-gcp-project-id"
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"

# Optional: Override default model
export GEMINI_MODEL="gemini-2.5-flash"
```

### Clone & Build

```bash
cd sentinel-ai/api

# Build the project
mvn clean compile

# Download dependencies offline (optional, for CI)
mvn dependency:go-offline -B
```

### Run Locally

**Option 1: Maven Exec Plugin**
```bash
mvn compile exec:java
```

**Option 2: Run with ADK Web Server**
```bash
mvn compile exec:java \
  -Dexec.args="--server.port=8080 \
    --adk.agents.source-dir=src/ \
    --logging.level.com.google.adk.dev=DEBUG"
```

The application starts on `http://localhost:8080` by default.

### Run with Docker

```bash
# Build Docker image
docker build -t sentinel-ai-api .

# Run container
docker run -p 8080:8080 \
  -e MCP_TOOLBOX_URL="http://host.docker.internal:5000/mcp/" \
  -e GOOGLE_CLOUD_PROJECT="your-project" \
  sentinel-ai-api
```

---

## 📁 Project Structure

```
api/
├── pom.xml                         # Maven build config (Java 17, ADK deps)
├── Dockerfile                      # Container build definition
├── src/
│   └── main/
│       └── java/
│           └── SoftwareBugAssistant.java   # Main agent entry point
└── README.md                       # This file
```

---

## 🔧 Dependencies

| Dependency              | Version | Purpose                          |
|-------------------------|---------|----------------------------------|
| `google-adk`            | 0.1.0   | Google ADK core framework        |
| `google-adk-dev`        | 0.1.0   | ADK development tools            |
| `jackson-databind`      | 2.17.2  | JSON serialization               |

---

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report
```

---

## 🐛 Troubleshooting

| Issue                              | Solution                                                      |
|------------------------------------|---------------------------------------------------------------|
| `MCP_TOOLBOX_URL not set`          | Export the env var or ensure MCP server is running on `:5000` |
| `Connection refused` to MCP        | Start MCP toolbox server: `mcp-toolbox serve`                 |
| `GOOGLE_APPLICATION_CREDENTIALS`   | Set path to valid GCP service account JSON                    |
| Maven build fails                  | Ensure Java 17+ is active: `java -version`                    |

