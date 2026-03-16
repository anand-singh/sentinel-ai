# 🧭 Orchestrator Service
_Sentinel (Agent ThinkING)_

The **Orchestrator** is the workflow “project manager” of Sentinel.  
It receives each incoming transaction event, **invokes the agents in the correct order**, forwards outputs from one step to the next, **handles retries/timeouts**, correlates logs, persists the resulting case, and updates the **Fraud Case Management Dashboard**.

> It **does not score** or **take actions** itself. It **coordinates** the pipeline:
> Pattern Analyzer → Behavioral Risk → Evidence Builder → Aggregated Scorer → Action Executor → Case/Dashboard.

---

## ✨ Responsibilities

- **Ingest** transactions (HTTP ingestion or Pub/Sub push subscription)
- **Sequence** and **fan‑out/fan‑in** calls to agent microservices
- **Propagate context** (transaction ID, correlation ID, policy/model versions)
- **Retry / timeout / fallback** per step (configurable)
- **Persist** the end‑to‑end result to Case Store (Firestore/AlloyDB)
- **Emit** dashboard updates (alerts list, case timeline)
- **Audit & trace**: structured logs, metrics, traces across all steps
- **Idempotency**: deduplicate re‑delivered events and repeated calls

---

## 🧱 End‑to‑End Flow

1. **Receive Event**  
   From Pub/Sub (preferred) or HTTP `/ingest`.

2. **Call Agent #1 — Pattern Analyzer**  
   Save `{risk_score, flags, reasoning}`.

3. **Call Agent #2 — Behavioral Risk**  
   Save `{behavioral_risk_score, flags, contributions}`.

4. **Call Agent #3 — Evidence Builder**  
   Provide outputs from 1 and 2 → receive `evidence_summary`, `combined_flags`.

5. **Call Agent #4 — Aggregated Scorer**  
   Provide outputs from 1–3 (+ optional AML). Get:
   - `final_risk_score`, `severity`, `recommended_action`.

6. **Call Agent #5 — Action Executor**  
   Execute policy‑approved tools. Collect `executed_actions`, `audit_id`.

7. **Persist & Notify**  
   Write case + timeline to **Firestore/AlloyDB**; ship analytics to **BigQuery**; notify the **Dashboard**.

---

## 🔗 Service Contracts (I/O)

### Ingestion (from upstream)
- **HTTP POST** `/ingest`  
  or **Pub/Sub** push endpoint `/events/transactions`

**Example incoming payload**
```json
{
  "transaction_id": "tx_123",
  "customer_id": "cust_456",
  "amount": 899.99,
  "currency": "EUR",
  "merchant_id": "m_789",
  "merchant_category": "ELECTRONICS",
  "channel": "CARD_PRESENT",
  "geo": { "lat": 52.37, "lon": 4.90, "country": "NL", "city": "Amsterdam" },
  "timestamp_utc": "2026-02-24T14:03:00Z",
  "reference_context": { /* globals for Agent #1 */ },
  "customer_profile_snapshot": { /* profile for Agent #2 */ }
}
````

### Outputs (to storage & UI)

*   **Case** (Firestore/AlloyDB):
    *   Transaction
    *   Agent outputs (1–4)
    *   Final decision (score/severity/recommendation)
    *   Actions executed + `audit_id`
    *   Evidence summary
    *   Policy/model/agent versions
    *   Timeline events (ordered)

*   **Dashboard Event** (Web/API):
    *   Alert card fields
    *   Case reference
    *   Timeline append (agent steps + actions)

***

## 🛣️ Orchestration Order & Error Handling

*   **Order** (default): `Pattern → Behavioral → Evidence → Aggregator → Executor`
*   **Timeouts**: per‑call (e.g., 2–5 s typical; configurable)
*   **Retries**: limited exponential backoff (idempotent calls only)
*   **Fallbacks**:
    *   If **Behavioral** is missing, continue with Pattern only
    *   If **Evidence** fails, fall back to minimal merged summary
    *   If **Aggregator** fails, mark `severity=MED` & `recommended_action=REVIEW`
    *   If **Executor** fails partially, record `PARTIAL_SUCCESS` and open a human task
*   **Dead‑letter**: push irrecoverable events to a DLQ (Pub/Sub topic) with full context

***

## ⚙️ Configuration

`application.yaml` (app basics), plus these configs:

*   `config/orchestrator.yaml`
    *   `timeouts` per agent
    *   `retries` (max attempts, backoff)
    *   `paths` (agent endpoints)
    *   `fanout` options (parallel Pattern/Behavioral)
    *   `dashboard_notify` toggle
*   `config/security.yaml`
    *   Outbound service accounts / IAM audiences
    *   Allow‑lists for agent endpoints
*   `config/observability.yaml`
    *   Log formats / sampling
    *   Metrics (histograms, counters)
    *   Tracing: exporter & sampling

> Surface `orchestrator_config_version` in logs & case records.

***

## 🔒 Security

*   **IAM‑to‑IAM** service calls (or mTLS) between Orchestrator and agents
*   **Least privilege** credentials per agent
*   **Input validation** on `/ingest` (strict schema; reject unknown fields)
*   **Mask PII** in logs; use correlation IDs
*   **Idempotency**: compute event hash (txnId+timestamp+amount+merchant) to dedupe

***

## 📊 Observability & Audit

*   **Structured logs** (JSON): correlation ID, step, latency, outcome, versions
*   **Metrics**:
    *   Step latencies (p50/p95)
    *   Error/timeout rates per agent
    *   DLQ count
    *   End‑to‑end latency
*   **Tracing**:
    *   Single trace across all agent calls
    *   Span per agent + storage + dashboard
*   **Audit**:
    *   Immutable append for each step (who/what/when/policyVersion)

***

## 🧪 Testing Strategy

*   **Unit tests**: step runner, retries, timeouts, fallbacks
*   **Contract tests**: agent request/response schemas
*   **Golden tests**: canonical LOW/MED/HIGH/CRITICAL flows (stable outcomes)
*   **Fault‑injection tests**:
    *   Agent timeouts, 5xx
    *   Missing optional agent (AML)
    *   Partial Executor failure
*   **Idempotency tests**: duplicate delivery → single case persisted

***

## 🧱 Tech Stack

*   **Language**: Java 17+
*   **Build**: Maven 3.9+
*   **Framework**: Spring Boot 3.x (Web, Validation, Actuator), WebClient/HTTP
*   **Messaging**: Pub/Sub (push or pull), optional HTTP ingest
*   **Pipelines (optional)**: Dataflow for ETL/pre‑enrichment
*   **Storage**: Firestore or AlloyDB (cases), BigQuery (analytics)
*   **Cloud Runtime**: Cloud Run (recommended)
*   **Observability**: Cloud Logging/Monitoring (Micrometer/OpenTelemetry)
*   **Security**: IAM, OIDC, service‑to‑service auth

***

## 🧩 Public API (Orchestrator)

    POST /ingest                    # Receive a transaction (alt to Pub/Sub push)
    POST /events/transactions       # Pub/Sub push endpoint (validated)
    GET  /healthz                   # Liveness
    GET  /readyz                    # Readiness

> All agent calls are **internal** and configured via `config/orchestrator.yaml`.

***

## ▶️ Local Development

```bash
# Compile + unit tests
mvn -pl orchestrator-java -am clean verify

# Run locally
mvn -pl orchestrator-java spring-boot:run

# Send a sample transaction
curl -sS -X POST http://localhost:8080/ingest \
  -H "Content-Type: application/json" \
  -d @docs/samples/transaction.json | jq .
```

***

## 🗂️ Suggested Folder Layout

    orchestrator-java
     ├─ src/main/java/.../api/IngressController.java
     ├─ src/main/java/.../core/OrchestrationService.java
     ├─ src/main/java/.../core/steps/
     │   ├─ PatternStep.java
     │   ├─ BehavioralStep.java
     │   ├─ EvidenceStep.java
     │   ├─ AggregateStep.java
     │   └─ ExecuteStep.java
     ├─ src/main/java/.../client/
     │   ├─ PatternClient.java
     │   ├─ BehavioralClient.java
     │   ├─ EvidenceClient.java
     │   ├─ AggregatorClient.java
     │   └─ ExecutorClient.java
     ├─ src/main/java/.../store/{CaseStore.java,FirestoreCaseStore.java,AlloyDbCaseStore.java}
     ├─ src/main/java/.../model/{IngressRequest.java,CaseRecord.java,TimelineEvent.java}
     ├─ src/main/resources/config/{orchestrator.yaml,security.yaml,observability.yaml}
     ├─ src/test/java/... (unit + golden + fault-injection + contract tests)
     └─ pom.xml

***

## ☁️ Deploy (Cloud Run Example)

```bash
# Build with Jib
mvn -pl orchestrator-java -DskipTests package \
  jib:build -Djib.to.image=eu.gcr.io/$GCP_PROJECT_ID/orchestrator:$(git rev-parse --short HEAD)

# Deploy
gcloud run deploy orchestrator \
  --image eu.gcr.io/$GCP_PROJECT_ID/orchestrator:$(git rev-parse --short HEAD) \
  --region $GCP_REGION \
  --allow-unauthenticated=false \
  --set-env-vars "APP_ENV=prod,ORCH_CONFIG=/app/config/orchestrator.yaml"
```

***

## 🔒 Compliance & Governance

*   Deterministic orchestration paths (versioned configs)
*   Full audit trail: each step → append‑only timeline entry
*   Correlation IDs across agents and storage records
*   Config review process for timeouts, retries, fallbacks
*   PII minimization: only necessary fields propagated; hashed IDs in logs

***

## 🧭 Roadmap

*   **Parallelization**: run Pattern & Behavioral in parallel to reduce latency
*   **Circuit breakers** per agent
*   **Adaptive timeouts** based on SLOs
*   **Replay tool** for DLQ events
*   **Policy‑based routing** (product/segment specific flows)
*   **Multi‑region failover** (active‑active Pub/Sub, regional stores)

***

## 📜 License

Specify your license in `LICENSE` (MIT/Apache‑2.0/Proprietary).
