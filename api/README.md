# Sentinel AI - API

Java/Maven API service using Google ADK for multi-agent fraud defense.

---

## 🛡️ Agents

### Transaction Pattern Analyzer

The first agent in Sentinel's fraud pipeline. Analyzes individual transactions against global & contextual patterns (amount, merchant, location, time, velocity) and emits a risk signal with clear flags and deterministic reasoning.

> This agent **does not take actions**. It only **scores & explains**.

**Tools:**

| Tool | Description |
|------|-------------|
| `analyze_amount_spike` | Detects unusual transaction amounts using z-score |
| `analyze_geo_distance` | Checks for impossible travel patterns |
| `analyze_velocity` | Analyzes transaction frequency bursts |
| `analyze_rare_mcc` | Detects unusual merchant categories |
| `analyze_time_window` | Checks for unusual transaction times |
| `blend_risk_scores` | Combines all signals into final risk score |

**Output:**

```json
{
  "risk_score": 86,
  "severity": "CRITICAL",
  "flags": ["AMOUNT_SPIKE", "GEO_MISMATCH"],
  "reasoning": "Anomalies detected: unusual amount (+12.3 pts), impossible travel (+18.5 pts). Total risk: 86/100",
  "recommendation": "BLOCK_AND_NOTIFY",
  "feature_contributions": { ... },
  "version": "pattern-v1.0.0"
}
```

---

### Behavioral Risk Agent

The second agent in Sentinel's fraud pipeline. Scores how unusual a transaction is for a **specific customer** by comparing it with that customer's historical behavior (amount ranges, active hours, geo patterns, device/IP, merchant mix, velocity).

> This agent **does not execute actions**. It only **scores & explains**.

**Tools:**

| Tool | Description |
|------|-------------|
| `analyze_amount_deviation` | Compares amount to customer's personal baseline (z-score) |
| `analyze_time_deviation` | Checks if time is during customer's sleep hours |
| `analyze_geo_deviation` | Checks distance/speed from customer's last location |
| `analyze_new_device` | Detects new device fingerprint for this customer |
| `analyze_new_ip_range` | Detects IP from unusual range for this customer |
| `analyze_merchant_novelty` | Checks if merchant/MCC is unusual for this customer |
| `analyze_burst_activity` | Checks if velocity exceeds customer's baseline |
| `blend_behavioral_scores` | Combines all signals into final behavioral risk score |

**Output:**

```json
{
  "behavioral_risk_score": 77,
  "flags": ["NEW_DEVICE", "UNUSUAL_TIME", "GEO_DEVIATION"],
  "feature_contributions": {
    "amount_zscore_customer": 3.2,
    "hour_deviation": 1.0,
    "geo_distance_km": 520.5,
    "new_device": 1,
    "new_ip_range": 0,
    "burst_in_window": 0
  },
  "reasoning": "Amount 3.2σ above customer's normal, new device, off-hours, travel 520km from last txn.",
  "version": "behavior-v1.0.0"
}
```

---

## 🚀 Getting Started (Development)

### Prerequisites

| Requirement          | Version | Notes                                    |
|----------------------|---------|------------------------------------------|
| **Java JDK**         | 17+     | OpenJDK or Oracle JDK                    |
| **Maven**            | 3.8+    | Build & dependency management            |
| **Docker**           | 20+     | (Optional) For containerized development |
| **Google Cloud SDK** | Latest  | For Vertex AI / ADK integrations         |

### Environment Variables

```bash
# Google Cloud / Vertex AI (required for Gemini models)
export GOOGLE_CLOUD_PROJECT="your-gcp-project-id"
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"

# Optional: MCP Toolbox URL (for Cloud SQL integration)
export MCP_TOOLBOX_URL="http://127.0.0.1:5000/mcp/"
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

**Transaction Pattern Analyzer:**
```bash
mvn compile exec:java@pattern-analyzer
```

**Behavioral Risk Agent:**
```bash
mvn compile exec:java@behavioral-risk
```

**With ADK Web Server:**
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
  -e GOOGLE_CLOUD_PROJECT="your-project" \
  sentinel-ai-api
```

---

## 📁 Project Structure

```
api/
├── pom.xml
├── Dockerfile
└── src/main/java/
    ├── SoftwareBugAssistant.java
    └── com/ing/sentinel/
        ├── agents/
        │   ├── TransactionPatternAnalyzer.java
        │   └── BehavioralRiskAgent.java
        └── tools/
            ├── AmountSpikeTool.java
            ├── GeoDistanceTool.java
            ├── VelocityTool.java
            ├── RareMccTool.java
            ├── TimeWindowTool.java
            ├── ScoreBlenderTool.java
            └── behavioral/
                ├── AmountDeviationSignal.java
                ├── TimeDeviationSignal.java
                ├── GeoDeviationSignal.java
                ├── NewDeviceSignal.java
                ├── NewIpRangeSignal.java
                ├── MerchantNoveltySignal.java
                ├── BurstActivitySignal.java
                └── BehavioralScoreBlender.java
```

---

## 🔧 Dependencies

| Dependency         | Version | Purpose                    |
|--------------------|---------|----------------------------|
| `google-adk`       | 0.6.0   | Google ADK core framework  |
| `google-adk-dev`   | 0.6.0   | ADK development tools      |
| `jackson-databind` | 2.17.2  | JSON serialization         |

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

| Issue                            | Solution                                              |
|----------------------------------|-------------------------------------------------------|
| `GOOGLE_APPLICATION_CREDENTIALS` | Set path to valid GCP service account JSON            |
| Maven build fails                | Ensure Java 17+ is active: `java -version`            |
| Agent not responding             | Check Gemini API quota and network connectivity       |
