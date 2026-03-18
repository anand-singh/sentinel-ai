# Sentinel AI

**Sentinel AI** is a production-grade, multi-agent fraud detection and case management platform for banking. It ingests financial transactions in real time, runs them through a 5-agent AI pipeline powered by Google ADK and Gemini, assigns risk scores, executes policy-governed actions, and surfaces everything in a live Fraud Case Management Dashboard.

## 🚀 Live Deployment

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/anand-singh/sentinel-ai/actions)
[![Cloud Run - API](https://img.shields.io/badge/Cloud%20Run-API%20Deployed-4285F4?logo=google-cloud)](https://sentinel-api-113270635078.europe-west4.run.app/health)
[![Cloud Run - Web](https://img.shields.io/badge/Cloud%20Run-Web%20Deployed-4285F4?logo=google-cloud)](https://sentinel-web-113270635078.europe-west4.run.app/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js)](https://nextjs.org/)
[![Google ADK](https://img.shields.io/badge/Google%20ADK-0.9.0-blue)](https://github.com/google/genai-adk)
[![Gemini](https://img.shields.io/badge/Gemini-2.5%20Flash-8E75B2?logo=google)](https://ai.google.dev/)

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              FRAUD DETECTION AGENTS (Google ADK + Gemini)                   │
│                                                                             │
│  Agent #1: TransactionPatternAnalyzer                                       │
│                                                                             │
│  Agent #2: BehavioralRiskAgent                                              │
│                                                                             │
│  Agent #3: EvidenceBuilder                                                  │
│                                                                             │
│  Agent #4: AggregatedRiskScorer                                             │
│                                                                             │
│  Agent #5: ActionExecutor                                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        GOOGLE CLOUD PLATFORM                                │
│                                                                             │
│   Vertex AI         Gemini 2.5 Flash (via google-genai SDK)                 │
│   Cloud Run         Future API service deployment                           │
│   Secret Manager    Service account keys                                    │
│   Cloud Logging     Agent execution logs                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Agent Pipeline

| #   | Agent                          | Status         | Responsibility                                                                               | Tools Count                 |
| --- | ------------------------------ | -------------- | -------------------------------------------------------------------------------------------- | --------------------------- |
| —   | **SentinelOrchestrator**       | ✅ Implemented | Coordinates the pipeline, invokes agents #1-5 in sequence                                    | 5+ coordination tools       |
| 1   | **TransactionPatternAnalyzer** | ✅ Implemented | Detect global anomalies: unusual amounts, rare merchants, impossible travel, velocity bursts | 6 tools                     |
| 2   | **BehavioralRiskDetector**     | ✅ Implemented | Score deviation from customer's personal historical behavior                                 | 8 tools                     |
| 3   | **EvidenceBuilderAgent**       | ✅ Implemented | Produce deterministic, audit-ready explanation bundle                                        | Evidence building tools     |
| 4   | **AggregatedRiskScorer**       | ✅ Implemented | Combine pattern + behavioral scores with policy weights into final 0–100 score               | Scoring & calibration tools |
| 5   | **ActionExecutor**             | ✅ Implemented | Execute policy-approved actions via explicit tools                                           | Action execution tools      |

---

## Tech Stack

| Layer         | Technology                                                          |
| ------------- | ------------------------------------------------------------------- |
| AI / Agents   | Google ADK 0.9.0, Gemini 2.5 Flash                                  |
| Agent Runtime | Java 17, Maven 3.8+, ADK InMemoryRunner                             |
| Agent Tools   | FunctionTool, deterministic calculations (z-score, Haversine, etc.) |
| Frontend      | Next.js 16, React 19, TypeScript 5                                  |
| Cloud         | Google Cloud Platform, Vertex AI API                                |
| Containers    | Docker, eclipse-temurin:17-jre-alpine                               |

---

## Getting Started

### Prerequisites

- **Java 17+**, Maven 3.8+
- **Google Cloud Project** with Vertex AI API enabled
- **Service Account JSON** with Vertex AI permissions

### Run the Full Pipeline

```bash
cd api
export GOOGLE_CLOUD_PROJECT="your-project-id"
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"

mvn compile exec:java@orchestrator
```

**Example interaction:**

```
You > Analyze transaction: customer=CUST-123, amount=1500 EUR,
      merchant=electronics, country=NG, time=2026-03-17T03:00:00Z

Agent > 🚀 Running 5-agent pipeline...
        ✅ Pattern Analyzer: risk_score=86, flags=[AMOUNT_SPIKE, GEO_MISMATCH]
        ✅ Behavioral Risk: behavioral_score=77, flags=[NEW_DEVICE, UNUSUAL_TIME]
        ✅ Evidence Builder: evidence compiled
        ✅ Aggregated Scorer: final_score=92, severity=CRITICAL
        ✅ Action Executor: BLOCK_AND_NOTIFY executed

        📊 Final Decision: BLOCK transaction, notify security team
```

### Run Individual Agents

```bash
cd sentinel-ai-platform/sentinel-ai-agent

mvn compile exec:java

Go to http://localhost:8080/ to interact with individual agents via web.
```

---

## REST API Reference

### Alerts

```
GET /api/alerts?page=1&limit=10&severity=CRITICAL&status=REVIEWING&q=sarah
```

Response:

```json
{
  "data": [
    {
      "id": "ALERT-001",
      "transactionId": "TX-9204",
      "customerId": "CUST-SJ01",
      "customerName": "Sarah Jenkins",
      "finalRiskScore": 85,
      "severity": "CRITICAL",
      "status": "REVIEWING",
      "recommendedAction": "freeze_transaction",
      "timestamp": "2026-03-17T10:00:00Z",
      "caseId": "F-9204"
    }
  ],
  "meta": { "page": 1, "limit": 10, "total": 6, "totalPages": 1 }
}
```

### Cases

```
GET  /api/cases/{id}
POST /api/cases/{id}/assign     { "assignTo": "Marcus Vance" }
POST /api/cases/{id}/note       { "content": "Reviewed login history" }
POST /api/cases/{id}/escalate
POST /api/cases/{id}/close
```

Case states: `OPEN` → `IN_REVIEW` → `ESCALATED` → `CLOSED`

Every mutation appends to `auditTrail[]` with timestamp, actor, and policy version.

### Analytics

```
GET /api/analytics
```

Returns: `alertsBySeverity[]`, `trendsLast30Days[]`, `topFlags[]`, `avgTimeToCloseHours`, `escalationRatePct`

### Ingest

```
POST /api/ingest
Content-Type: application/json
```

Body: raw transaction JSON. Triggers the full 5-agent pipeline. Timeout: 90 s.
Returns: complete `CaseDetail` of the newly created case.

### Health

```
GET /health
→ { "status": "UP" }
```

---

## Dashboard Features

| Page            | Description                                                                                                                                                          |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Dashboard**   | Live KPI cards · risk score gauge · fraud trend chart · agent reason chain (with Approve / Reject Case buttons) · recent alerts queue (rows navigate to case detail) |
| **Case Detail** | Full case view: alert metadata · per-agent outputs (tabbed) · executed actions · interactive audit timeline · quick-action panel (assign / note / escalate / close)  |
| **Analytics**   | Alerts by severity · 30-day trend · top fraud flags · avg time-to-close · escalation rate                                                                            |
| **Admin**       | Policy version · agent versions · alert routing thresholds · system config (read-only)                                                                               |

**Ingest modal** — Submit a raw transaction JSON from the dashboard. The 5-agent pipeline runs and the new case opens automatically.

**Approve / Reject** buttons on the dashboard reason chain call `POST /api/cases/F-9204/close` and `POST /api/cases/F-9204/escalate` with live loading and success states.

---

## Deployment

Both services deploy automatically to Cloud Run via GitHub Actions on push to `main`.

### Required GitHub Secrets

| Secret             | Description                               |
| ------------------ | ----------------------------------------- |
| `GCP_PROJECT_ID`   | Google Cloud project ID                   |
| `GCP_SA_KEY`       | GitHub Actions service account key (JSON) |
| `SENTINEL_API_URL` | Public Cloud Run URL of `sentinel-api`    |

### Cloud Run service configuration

| Service        | CPU | Memory | Min instances | Max instances | Timeout |
| -------------- | --- | ------ | ------------- | ------------- | ------- |
| `sentinel-api` | 2   | 2 Gi   | 0             | 8             | 300 s   |
| `sentinel-web` | 1   | 512 Mi | 0             | 4             | 60 s    |

`GEMINI_API_KEY` is injected from Secret Manager at runtime — never stored in environment variables or source code.

---

## Environment Variables

### Java API (`sentinel-api`)

| Variable         | Default                    | Description                            |
| ---------------- | -------------------------- | -------------------------------------- |
| `PORT`           | `8080`                     | HTTP server port                       |
| `GEMINI_API_KEY` | —                          | Gemini API key (required for pipeline) |
| `GEMINI_MODEL`   | `gemini-2.5-flash`         | Gemini model name                      |
| `LOG_LEVEL`      | `INFO`                     | Application log level                  |
| `POLICY_VERSION` | `action-policy-2026-03-16` | Active action policy version           |

### Next.js Frontend (`sentinel-web`)

| Variable           | Default | Description                                    |
| ------------------ | ------- | ---------------------------------------------- |
| `SENTINEL_API_URL` | `""`    | Java API base URL. Empty = use in-memory mock. |

---

## Design Principles

✅ **Tool-based agents** — All fraud detection logic implemented as explicit FunctionTools, not in prompts

✅ **Deterministic signals** — Pattern and behavioral tools use deterministic calculations (z-score, Haversine distance, CIDR matching)

✅ **Explainability first** — Every agent provides reasoning with feature contributions and version info for audit trail

✅ **Strict role separation** — Each agent has a single responsibility. Pattern analyzes global signals, Behavioral analyzes customer-specific signals.

✅ **Version tracking** — All agent outputs include version numbers (agent version + config version) for compliance

✅ **Stateless & testable** — Agents have no session state, can be tested independently via interactive CLI

✅ **Policy-governed actions** — Future Action Executor will only call explicitly registered tools, cannot improvise

✅ **Observable** — Structured logging with agent versions, tool execution details, and timing information
