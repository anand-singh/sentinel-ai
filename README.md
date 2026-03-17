# Sentinel AI

**Sentinel AI** is a production-grade, multi-agent fraud detection and case management platform for banking. It ingests financial transactions in real time, runs them through a 5-agent AI pipeline powered by Google ADK and Gemini, assigns risk scores, executes policy-governed actions, and surfaces everything in a live Fraud Case Management Dashboard.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            ANALYST BROWSER                                  │
│                                                                              │
│   ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌──────────────────┐  │
│   │  Dashboard  │  │ Case Detail  │  │ Analytics  │  │ Ingest / Analyze │  │
│   └──────┬──────┘  └──────┬───────┘  └─────┬──────┘  └────────┬─────────┘  │
└──────────┼────────────────┼────────────────┼──────────────────┼────────────┘
           │  browser calls /api/* routes    │                  │
           ▼                ▼                ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│               NEXT.JS FRONTEND  (Cloud Run: sentinel-web)                   │
│                                                                              │
│   Next.js 16 App Router · React 19 · TypeScript · Tailwind CSS 4           │
│                                                                              │
│   API Routes (BFF / proxy layer — keeps CORS browser-safe)                 │
│     GET  /api/alerts                GET  /api/analytics                     │
│     GET  /api/cases/[id]            POST /api/ingest                        │
│     POST /api/cases/[id]/assign|note|escalate|close                         │
│                                                                              │
│   env: SENTINEL_API_URL ─────────────────────────────────────────────┐     │
└──────────────────────────────────────────────────────────────────────┼─────┘
                                                                        │
                      server-to-server (no CORS)                        │
                                                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│               SPRING BOOT REST API  (Cloud Run: sentinel-api)               │
│                                                                              │
│   Java 17 · Spring Boot 4.0.2 · Google ADK 0.9.0 · Jackson 2.17           │
│                                                                              │
│   SentinelApiController                                                     │
│     GET  /health                                                             │
│     GET  /api/alerts          (paginated, filterable)                       │
│     GET  /api/cases/{id}                                                    │
│     POST /api/cases/{id}/assign|note|escalate|close                         │
│     GET  /api/analytics                                                     │
│     POST /api/ingest ───────────────────────────────────────────────┐      │
│                                                                      │      │
│   CaseStoreService  (ConcurrentHashMap, pre-seeded)                  │      │
│     alerts: Map<alertId → Alert>                                     │      │
│     cases:  Map<caseId  → CaseDetail>                                │      │
└──────────────────────────────────────────────────────────────────────┼──────┘
                                                                       │
                         OrchestratorService                           │
                         (ADK InMemoryRunner)                          │
                                                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                   5-AGENT FRAUD DETECTION PIPELINE                          │
│                       (Google ADK + Gemini 2.5 Flash)                       │
│                                                                              │
│  Transaction JSON                                                            │
│       │                                                                      │
│       ▼                                                                      │
│  ┌──────────────────────────────────────────────────────────────────┐       │
│  │  SentinelOrchestrator  (coordinator — does not score or act)     │       │
│  │  Runs agents #1–5 in sequence via ADK InMemoryRunner + Gemini    │       │
│  └──┬───────────────────────────────────────────────────────────────┘       │
│     │                                                                        │
│     ▼                                                                        │
│  Agent #1  TransactionPatternAnalyzer                                        │
│  Tools:    AmountSpike · GeoDistance · RareMcc · Velocity ·                 │
│            TimeWindow · ScoreBlender                                         │
│  Output:   risk_score (0–100) · flags[] · reasoning                         │
│     │                                                                        │
│     ▼                                                                        │
│  Agent #2  BehavioralRiskDetector                                            │
│  Tools:    AmountDeviation · TimeDeviation · GeoDeviation · NewDevice ·     │
│            NewIpRange · MerchantNovelty · BurstActivity · ScoreBlender      │
│  Output:   behavioral_risk_score · flags[] · contributions                  │
│     │                                                                        │
│     ▼                                                                        │
│  Agent #3  EvidenceBuilderAgent                                              │
│  Tools:    FlagMerger · SummaryComposer · EvidenceBuilder                   │
│  Output:   evidence_summary · combined_flags[] · audit bundle               │
│     │                                                                        │
│     ▼                                                                        │
│  Agent #4  AggregatedRiskScorer                                              │
│  Tools:    RiskBooster · ScoreCalibrator · ScoreNormalizer ·                │
│            SeverityClassifier · WeightedScoreBlender                         │
│  Output:   final_risk_score · severity (LOW/MED/HIGH/CRITICAL) ·            │
│            recommended_action                                                │
│     │                                                                        │
│     ▼                                                                        │
│  Agent #5  ActionExecutor                                                    │
│  Tools:    FreezeTransaction · NotifySecurityTeam · RequestStepUpAuth ·     │
│            EscalateToHuman · CreateCaseReport                                │
│  Output:   executed_actions[] · audit_id                                    │
│     │                                                                        │
│     └──────────────► OrchestratorResult JSON                                │
│                             │                                                │
│                             ▼                                                │
│                   CaseStoreService.putCase()                                 │
│                   CaseStoreService.putAlert()                                │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        GOOGLE CLOUD PLATFORM                                │
│                                                                              │
│   Cloud Run         sentinel-api · sentinel-web                             │
│   Artifact Registry sentinel-ai-images                                      │
│   Secret Manager    gemini-api-key                                          │
│   Cloud Logging     structured logs from both services                      │
│   GitHub Actions    deploy-api.yml · deploy-web.yml (push to main)          │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Agent Pipeline

| # | Agent | Responsibility | Key Tools |
|---|-------|----------------|-----------|
| — | **SentinelOrchestrator** | Sequence coordinator. Calls agents #1–5, propagates context, returns consolidated result. Does not score or act. | RunPatternAnalyzer, RunBehavioralRisk, RunEvidenceBuilder, RunAggregatedScorer, RunActionExecutor |
| 1 | **TransactionPatternAnalyzer** | Detect global anomalies: unusual amounts, rare merchants, location shifts, transaction velocity | AmountSpike, GeoDistance, RareMcc, Velocity, TimeWindow, ScoreBlender |
| 2 | **BehavioralRiskDetector** | Score deviation from the customer's own historical behaviour | AmountDeviation, TimeDeviation, GeoDeviation, NewDevice, NewIpRange, MerchantNovelty, BurstActivity |
| 3 | **EvidenceBuilderAgent** | Produce a deterministic, audit-ready explanation bundle before any action is taken | FlagMerger, SummaryComposer, EvidenceBuilder |
| 4 | **AggregatedRiskScorer** | Combine signals into a calibrated 0–100 risk score with severity and recommended action | RiskBooster, ScoreCalibrator, ScoreNormalizer, SeverityClassifier, WeightedScoreBlender |
| 5 | **ActionExecutor** | Execute only explicitly registered, policy-approved tools — cannot improvise | FreezeTransaction, NotifySecurityTeam, RequestStepUpAuth, EscalateToHuman, CreateCaseReport |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| AI / Agents | Google ADK 0.9.0, Gemini 2.5 Flash |
| Backend | Java 17, Spring Boot 4.0.2, Maven 3.9 |
| Frontend | Next.js 16 (App Router), React 19, TypeScript 5 |
| Styling | Tailwind CSS 4, shadcn/ui, Lucide icons |
| Observability | OpenTelemetry SDK 1.43.0, Cloud Logging |
| Containers | Docker multi-stage, node:20-alpine, eclipse-temurin:17-jre-alpine |
| Cloud | Google Cloud Run, Artifact Registry, Secret Manager |
| CI/CD | GitHub Actions |
| Testing | Jest 30, React Testing Library |

---

## Project Structure

```
sentinel-ai/
├── api/                                  # Java Spring Boot backend
│   ├── src/main/java/com/ing/sentinel/
│   │   ├── SentinelApplication.java      # @SpringBootApplication entry point
│   │   ├── agents/
│   │   │   ├── SentinelOrchestrator.java # Pipeline coordinator (ROOT_AGENT)
│   │   │   ├── TransactionPatternAnalyzer.java
│   │   │   ├── BehavioralRiskDetector.java
│   │   │   ├── EvidenceBuilderAgent.java
│   │   │   ├── AggregatedRiskScorer.java
│   │   │   └── ActionExecutor.java
│   │   ├── api/
│   │   │   ├── SentinelApiController.java  # REST endpoints
│   │   │   └── HealthController.java
│   │   ├── service/
│   │   │   └── OrchestratorService.java    # ADK InMemoryRunner wrapper
│   │   ├── store/
│   │   │   └── CaseStoreService.java       # In-memory case/alert store
│   │   ├── config/
│   │   │   ├── CorsConfig.java
│   │   │   └── TracingConfig.java
│   │   └── tools/
│   │       ├── pattern/                    # Agent #1 tools
│   │       ├── behavioral/                 # Agent #2 tools
│   │       ├── evidence/                   # Agent #3 tools
│   │       ├── aggregator/                 # Agent #4 tools
│   │       ├── action/                     # Agent #5 tools
│   │       └── orchestrator/               # Pipeline coordination tools
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── web/                                  # Next.js frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── page.tsx                  # Dashboard (home)
│   │   │   ├── cases/[id]/page.tsx       # Case detail
│   │   │   ├── analytics/page.tsx        # Analytics dashboard
│   │   │   ├── admin/page.tsx            # Policy / config viewer
│   │   │   ├── layout.tsx
│   │   │   └── api/                      # Next.js API proxy (BFF)
│   │   │       ├── alerts/route.ts
│   │   │       ├── cases/[id]/route.ts
│   │   │       ├── cases/[id]/assign/route.ts
│   │   │       ├── cases/[id]/note/route.ts
│   │   │       ├── cases/[id]/escalate/route.ts
│   │   │       ├── cases/[id]/close/route.ts
│   │   │       ├── analytics/route.ts
│   │   │       └── ingest/route.ts
│   │   ├── components/
│   │   │   ├── dashboard/
│   │   │   │   ├── SummaryCards.tsx
│   │   │   │   ├── CaseRiskScore.tsx
│   │   │   │   ├── FraudTrends.tsx
│   │   │   │   ├── ReasonChain.tsx       # Agent reason chain + approve/reject
│   │   │   │   ├── RecentAlerts.tsx      # Live alert table with row navigation
│   │   │   │   └── IngestTransactionModal.tsx
│   │   │   ├── cases/
│   │   │   │   ├── AgentTabs.tsx
│   │   │   │   ├── AuditTimeline.tsx
│   │   │   │   └── QuickActions.tsx
│   │   │   └── layout/
│   │   │       ├── DashboardLayout.tsx
│   │   │       ├── Header.tsx
│   │   │       └── Sidebar.tsx
│   │   ├── lib/
│   │   │   ├── apiProxy.ts               # proxyGet / proxyPost helpers
│   │   │   └── mockDb.ts                 # Fallback in-memory data
│   │   └── types/alert.ts                # Alert, CaseDetail, Analytics types
│   ├── Dockerfile
│   ├── next.config.ts                    # output: standalone (Cloud Run)
│   └── package.json
│
├── .github/workflows/
│   ├── deploy-api.yml                    # Java API → Cloud Run (on api/** changes)
│   └── deploy-web.yml                    # Next.js → Cloud Run (on web/** changes)
│
└── doc/
    ├── orchestrator.md
    ├── dashboard.md
    └── agents/                           # Per-agent documentation
```

---

## Getting Started

### Prerequisites

- Java 17+, Maven 3.9+
- Node.js 20+
- A Gemini API key from [Google AI Studio](https://aistudio.google.com)

### Run the API

```bash
cd api

export GEMINI_API_KEY=your_key_here

mvn spring-boot:run
# Starts on http://localhost:8080
```

Verify:

```bash
curl http://localhost:8080/health
# {"status":"UP"}

curl http://localhost:8080/api/alerts
# {"data":[...],"meta":{...}}
```

### Run the Frontend

```bash
cd web

echo "SENTINEL_API_URL=http://localhost:8080" > .env.local

npm install
npm run dev
# Opens at http://localhost:3000
```

The frontend falls back to in-memory mock data when `SENTINEL_API_URL` is not set — useful for UI-only development.

### Run the full pipeline

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-1234",
    "customer_id": "CUST-001",
    "customer_name": "Jane Doe",
    "amount": 4950.00,
    "merchant": "Unknown Electronics",
    "country": "NG",
    "timestamp": "2026-03-17T12:00:00Z"
  }'
```

Returns the full `CaseDetail` after the 5-agent pipeline completes (~30–90 s).

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

| Page | Description |
|------|-------------|
| **Dashboard** | Live KPI cards · risk score gauge · fraud trend chart · agent reason chain (with Approve / Reject Case buttons) · recent alerts queue (rows navigate to case detail) |
| **Case Detail** | Full case view: alert metadata · per-agent outputs (tabbed) · executed actions · interactive audit timeline · quick-action panel (assign / note / escalate / close) |
| **Analytics** | Alerts by severity · 30-day trend · top fraud flags · avg time-to-close · escalation rate |
| **Admin** | Policy version · agent versions · alert routing thresholds · system config (read-only) |

**Ingest modal** — Submit a raw transaction JSON from the dashboard. The 5-agent pipeline runs and the new case opens automatically.

**Approve / Reject** buttons on the dashboard reason chain call `POST /api/cases/F-9204/close` and `POST /api/cases/F-9204/escalate` with live loading and success states.

---

## Deployment

Both services deploy automatically to Cloud Run via GitHub Actions on push to `main`.

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `GCP_PROJECT_ID` | Google Cloud project ID |
| `GCP_SA_KEY` | GitHub Actions service account key (JSON) |
| `SENTINEL_API_URL` | Public Cloud Run URL of `sentinel-api` |

### Cloud Run service configuration

| Service | CPU | Memory | Min instances | Max instances | Timeout |
|---------|-----|--------|---------------|---------------|---------|
| `sentinel-api` | 2 | 2 Gi | 0 | 8 | 300 s |
| `sentinel-web` | 1 | 512 Mi | 0 | 4 | 60 s |

`GEMINI_API_KEY` is injected from Secret Manager at runtime — never stored in environment variables or source code.

---

## Environment Variables

### Java API (`sentinel-api`)

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | HTTP server port |
| `GEMINI_API_KEY` | — | Gemini API key (required for pipeline) |
| `GEMINI_MODEL` | `gemini-2.5-flash` | Gemini model name |
| `LOG_LEVEL` | `INFO` | Application log level |
| `POLICY_VERSION` | `action-policy-2026-03-16` | Active action policy version |

### Next.js Frontend (`sentinel-web`)

| Variable | Default | Description |
|----------|---------|-------------|
| `SENTINEL_API_URL` | `""` | Java API base URL. Empty = use in-memory mock. |

---

## Design Principles

**Policy-governed actions** — Agent #5 can only call explicitly registered tools. It cannot escalate, freeze, or notify outside its toolset, regardless of what earlier agents suggest.

**Explainability first** — Agent #3 builds a deterministic audit bundle before any action is taken. Every case includes a full reason chain traceable to raw signal values.

**Strict role separation** — Each agent has a single job. The orchestrator coordinates but never scores or acts. Tools within each agent are independently testable.

**Graceful degradation** — The frontend falls back to seeded mock data when the Java API is unreachable. All pages render correctly without a running backend.

**Immutable audit trail** — Every case mutation appends an audit entry with correlation ID, actor, timestamp, and policy version. Entries are append-only.
