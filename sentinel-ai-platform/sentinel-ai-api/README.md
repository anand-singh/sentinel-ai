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

### Evidence Builder Agent

The third agent in Sentinel's fraud pipeline. Combines outputs from upstream agents (Pattern Analyzer, Behavioral Risk, AML/Compliance) and produces clear, auditable, human-readable explanations.

> This agent **does not score or take actions**. It **aggregates, normalizes, and explains**.

**Tools:**

| Tool | Description |
|------|-------------|
| `merge_flags` | Combines flags from all upstream agents |
| `compose_summary` | Generates human-readable summary |
| `build_evidence_bundle` | Creates audit-ready evidence package |

**Output:**

```json
{
  "evidence_summary": "High amount (3.2σ above customer baseline), new device, transaction at 3am (customer's sleep hours), traveled 520km in 2 hours",
  "combined_flags": ["AMOUNT_SPIKE", "AMOUNT_DEVIATION", "NEW_DEVICE", "UNUSUAL_TIME", "GEO_DEVIATION"],
  "flag_sources": {
    "pattern_agent": ["AMOUNT_SPIKE", "GEO_MISMATCH"],
    "behavioral_agent": ["AMOUNT_DEVIATION", "NEW_DEVICE", "UNUSUAL_TIME"]
  },
  "audit_bundle": {
    "decision_tree": "...",
    "feature_importance": { ... }
  },
  "version": "evidence-v1.0.0"
}
```

---

### Aggregated Risk Scorer

The fourth agent in Sentinel's fraud pipeline. Combines outputs from all upstream agents and produces a single calibrated risk score (0-100), severity label, and recommended action.

> This agent **does not execute actions**. It only **recommends**. Actions are performed by Agent #5.

**Tools:**

| Tool | Description |
|------|-------------|
| `normalize_score` | Normalizes scores from different agents to common scale |
| `blend_weighted_scores` | Combines scores using configurable policy weights |
| `apply_risk_boost` | Applies multipliers for high-severity flag combinations |
| `calibrate_score` | Calibrates final score using historical data |
| `classify_severity` | Maps score to severity (MINIMAL/LOW/MEDIUM/HIGH/CRITICAL) |

**Output:**

```json
{
  "final_risk_score": 92,
  "severity": "CRITICAL",
  "recommended_action": "BLOCK_AND_NOTIFY",
  "score_breakdown": {
    "pattern_contribution": 35.2,
    "behavioral_contribution": 28.8,
    "evidence_contribution": 15.0,
    "aml_contribution": 13.0
  },
  "policy_weights": {
    "pattern": 0.40,
    "behavioral": 0.35,
    "evidence": 0.15,
    "aml": 0.10
  },
  "version": "aggregator-v1.0.0",
  "model_version": "agg-v1.0.0"
}
```

---

### Action Executor

The fifth and final agent in Sentinel's fraud pipeline. Receives final risk decisions from the Aggregated Risk Scorer and executes **ONLY** approved, policy-governed actions.

> This agent **does NOT score, explain, or improvise**. It executes **only explicitly registered tools** under strict policy control.

**Tools:**

| Tool | Description |
|------|-------------|
| `freeze_transaction` | Freezes/blocks the transaction immediately |
| `notify_security_team` | Sends alert to security operations team |
| `create_case_report` | Creates fraud case in case management system |
| `request_step_up_auth` | Requests additional authentication from customer |
| `escalate_to_human` | Escalates to human fraud analyst for review |

**Output:**

```json
{
  "executed_actions": [
    {
      "action": "freeze_transaction",
      "status": "SUCCESS",
      "transaction_id": "tx_123",
      "timestamp": "2026-03-17T14:05:32Z"
    },
    {
      "action": "notify_security_team",
      "status": "SUCCESS",
      "notification_id": "notif_789",
      "timestamp": "2026-03-17T14:05:33Z"
    },
    {
      "action": "create_case_report",
      "status": "SUCCESS",
      "case_id": "case_abc123",
      "timestamp": "2026-03-17T14:05:34Z"
    }
  ],
  "audit_id": "audit_xyz",
  "version": "executor-v1.0.0",
  "policy_version": "action-policy-2026-03-16"
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
└── src/main/java/com/ing/sentinel/
    ├── SentinelApplication.java          # Spring Boot main class
    ├── SentinelDevServer.java            # ADK Dev UI server
    ├── agents/
    │   ├── SentinelOrchestrator.java     # Pipeline coordinator
    │   ├── TransactionPatternAnalyzer.java
    │   ├── BehavioralRiskDetector.java
    │   ├── EvidenceBuilderAgent.java
    │   ├── AggregatedRiskScorer.java
    │   └── ActionExecutor.java
    ├── api/
    │   ├── SentinelApiController.java    # REST endpoints
    │   └── HealthController.java
    ├── service/
    │   └── OrchestratorService.java      # Service layer
    ├── store/
    │   └── CaseStoreService.java         # Case/alert storage
    ├── config/
    │   ├── CorsConfig.java
    │   └── TracingConfig.java            # OpenTelemetry config
    └── tools/
        ├── pattern/                      # Agent #1 tools
        │   ├── AmountSpikeTool.java
        │   ├── GeoDistanceTool.java
        │   ├── VelocityTool.java
        │   ├── RareMccTool.java
        │   ├── TimeWindowTool.java
        │   └── ScoreBlenderTool.java
        ├── behavioral/                   # Agent #2 tools
        │   ├── AmountDeviationSignal.java
        │   ├── TimeDeviationSignal.java
        │   ├── GeoDeviationSignal.java
        │   ├── NewDeviceSignal.java
        │   ├── NewIpRangeSignal.java
        │   ├── MerchantNoveltySignal.java
        │   ├── BurstActivitySignal.java
        │   └── BehavioralScoreBlender.java
        ├── evidence/                     # Agent #3 tools
        │   ├── FlagMergerTool.java
        │   ├── SummaryComposerTool.java
        │   ├── EvidenceBuilderTool.java
        │   └── ManualToolVerification.java
        ├── aggregator/                   # Agent #4 tools
        │   ├── ScoreNormalizer.java
        │   ├── WeightedScoreBlender.java
        │   ├── RiskBooster.java
        │   ├── ScoreCalibrator.java
        │   └── SeverityClassifier.java
        ├── action/                       # Agent #5 tools
        │   ├── FreezeTransactionTool.java
        │   ├── NotifySecurityTeamTool.java
        │   ├── CreateCaseReportTool.java
        │   ├── RequestStepUpAuthTool.java
        │   └── EscalateToHumanTool.java
        └── orchestrator/                 # Orchestrator tools
            ├── RunPatternAnalyzerTool.java
            ├── RunBehavioralRiskTool.java
            ├── RunEvidenceBuilderTool.java
            ├── RunAggregatedScorerTool.java
            └── RunActionExecutorTool.java
```

---

## 🔧 Dependencies

| Dependency         | Version | Purpose                    |
|--------------------|---------|----------------------------|
| `google-adk`       | 0.9.0   | Google ADK core framework  |
| `google-adk-dev`   | 0.9.0   | ADK development tools      |
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
