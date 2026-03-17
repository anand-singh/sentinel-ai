# Sentinel AI — Live Demo Guide

> **For evaluators:** This document walks through five realistic fraud scenarios that showcase
> the full capabilities of the Sentinel AI multi-agent pipeline. Each scenario demonstrates
> a different fraud pattern, a different risk outcome, and a different set of policy-governed actions.

---

## What You Are Looking At

Sentinel AI is a **production-grade fraud detection platform** built on **Google ADK** and **Gemini 2.5 Flash**. A single incoming transaction triggers a coordinated **5-agent AI pipeline** that:

1. Detects global transaction anomalies (Agent #1 — Pattern Analyzer)
2. Scores customer-specific behavioural deviation (Agent #2 — Behavioral Risk Detector)
3. Builds a deterministic, audit-ready evidence bundle (Agent #3 — Evidence Builder)
4. Produces a calibrated 0–100 risk score + severity classification (Agent #4 — Aggregated Risk Scorer)
5. Executes only explicitly registered, policy-approved actions (Agent #5 — Action Executor)

Every decision is explainable, traceable, and carries a correlation ID, agent version, and policy version — ready for regulatory audit.

---

## Setup (30 seconds)

```bash
# Terminal 1 — Java API (fraud pipeline + REST)
cd api
export GEMINI_API_KEY=your_key
mvn spring-boot:run
# → http://localhost:8080

# Terminal 2 — Next.js Dashboard
cd web
echo "SENTINEL_API_URL=http://localhost:8080" > .env.local
npm run dev
# → http://localhost:3000

# Optional: Terminal 3 — ADK Dev UI (watch agents live)
cd api
GEMINI_API_KEY=your_key mvn exec:java@dev-ui
# → http://localhost:8090
```

---

## Demo Scenario 1 — Geographic Impossibility (CRITICAL)

**Story:** Sarah Jenkins logs in from New York at 09:00, then a $4,950 card transaction appears from Lagos, Nigeria at 09:04. Physically impossible. New device fingerprint. 4 rapid transactions in 28 seconds.

**What it tests:** GEO_MISMATCH + AMOUNT_SPIKE + VELOCITY_CHECK_FAILED + NEW_DEVICE

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-DEMO-001",
    "customer_id": "CUST-SJ01",
    "customer_name": "Sarah Jenkins",
    "amount": 4950.00,
    "currency": "USD",
    "merchant": "ElectroHub Lagos",
    "merchant_category": "ELECTRONICS",
    "channel": "CARD_NOT_PRESENT",
    "country": "NG",
    "city": "Lagos",
    "device_id": "DEV-NEW-9921",
    "ip_address": "197.211.62.10",
    "timestamp": "2026-03-17T09:04:00Z",
    "customer_profile": {
      "home_country": "US",
      "home_city": "New York",
      "avg_transaction_amount": 145.00,
      "known_devices": ["DEV-IPHONE-001", "DEV-MACBOOK-002"],
      "last_login_location": "New York, US",
      "last_login_time": "2026-03-17T09:00:00Z"
    }
  }'
```

**Expected pipeline outcome:**
- Agent #1: `risk_score ≈ 88`, flags `GEO_MISMATCH`, `AMOUNT_SPIKE` (3,314% above avg), `VELOCITY_CHECK_FAILED`
- Agent #2: `behavioral_score ≈ 91`, flags `NEW_DEVICE`, `GEO_DEVIATION`, `BURST_ACTIVITY`
- Agent #3: Evidence bundle with 5 combined flags, human-readable summary
- Agent #4: `final_risk_score ≈ 90`, severity `CRITICAL`, recommended action `freeze_transaction`
- Agent #5: executes `freeze_transaction` + `notify_security_team` + `create_case_report`

**What to show in the dashboard:**
1. New alert appears at top of **Recent Alerts Queue** with `CRITICAL` severity badge
2. Navigate to the case → **Agent Tabs** show 4 tabs of reasoning
3. **Audit Timeline** shows 5 timestamped events from 3 agents
4. Click **Assign** → assign to yourself → status changes to `IN_REVIEW`

---

## Demo Scenario 2 — Account Takeover Probe (HIGH)

**Story:** Robert Chen's account shows 8 small test charges ($1–$5) at different merchants in 6 minutes — a classic card-testing pattern before a large fraudulent purchase. Unusual merchant categories, new IP range.

**What it tests:** VELOCITY_CHECK_FAILED + MERCHANT_NOVELTY + NEW_IP_RANGE (low individual amounts, but high velocity)

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-DEMO-002",
    "customer_id": "CUST-RC02",
    "customer_name": "Robert Chen",
    "amount": 3.99,
    "currency": "USD",
    "merchant": "Parking Meter App #8",
    "merchant_category": "PARKING",
    "channel": "CARD_NOT_PRESENT",
    "country": "US",
    "city": "San Francisco",
    "device_id": "DEV-RC-PHONE-01",
    "ip_address": "23.105.200.44",
    "timestamp": "2026-03-17T14:22:00Z",
    "recent_transactions_last_10min": 8,
    "customer_profile": {
      "home_country": "US",
      "home_city": "Seattle",
      "avg_transaction_amount": 210.00,
      "known_ip_ranges": ["98.196.x.x", "73.220.x.x"],
      "typical_merchants": ["Amazon", "Whole Foods", "Apple Store"]
    }
  }'
```

**Expected pipeline outcome:**
- Agent #1: `risk_score ≈ 72`, flags `VELOCITY_CHECK_FAILED`, `RARE_MCC`
- Agent #2: `behavioral_score ≈ 68`, flags `NEW_IP_RANGE`, `MERCHANT_NOVELTY`, `BURST_ACTIVITY`
- Agent #4: `final_risk_score ≈ 71`, severity `HIGH`, recommended `notify_security_team`
- Agent #5: executes `notify_security_team` + `request_step_up_auth`

**What to show in the dashboard:**
1. Alert with `HIGH` severity — notice no freeze (lower confidence than CRITICAL)
2. In Case Detail → **Agent Tabs** → Behavioral Risk tab shows 8 transactions in 10 min
3. Click **Add Note** → type: "Card testing pattern — monitor for large follow-up charge"
4. Click **Escalate** → status moves to `ESCALATED` → audit trail updates instantly

---

## Demo Scenario 3 — Legitimate High-Value Purchase (LOW → ALLOW)

**Story:** Emma Wilson books a business-class flight to Tokyo for $8,200. She travels frequently for work, regularly makes high-value purchases, and is on her known device from her usual location. The pipeline should clear this.

**What it tests:** That the system correctly identifies LOW risk and does NOT over-trigger — avoiding false positives is as important as catching fraud.

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-DEMO-003",
    "customer_id": "CUST-EW03",
    "customer_name": "Emma Wilson",
    "amount": 8200.00,
    "currency": "USD",
    "merchant": "British Airways",
    "merchant_category": "AIRLINES",
    "channel": "CARD_NOT_PRESENT",
    "country": "US",
    "city": "New York",
    "device_id": "DEV-EW-MACBOOK-01",
    "ip_address": "72.229.100.15",
    "timestamp": "2026-03-17T11:30:00Z",
    "customer_profile": {
      "home_country": "US",
      "home_city": "New York",
      "avg_transaction_amount": 1200.00,
      "known_devices": ["DEV-EW-MACBOOK-01", "DEV-EW-IPHONE-01"],
      "known_ip_ranges": ["72.229.x.x"],
      "frequent_merchants": ["British Airways", "Delta", "Marriott", "Amex Travel"],
      "international_travel_frequency": "monthly"
    }
  }'
```

**Expected pipeline outcome:**
- Agent #1: `risk_score ≈ 22`, flags `[]` (amount high but consistent with profile)
- Agent #2: `behavioral_score ≈ 18`, flags `[]` (known device, known merchant category)
- Agent #4: `final_risk_score ≈ 20`, severity `LOW`, recommended `create_case_report`
- Agent #5: executes `create_case_report` only — no freeze, no notification

**What to show in the dashboard:**
1. Alert with `LOW` severity (green dot) — RESOLVED status
2. Highlight: **no freeze action taken** — policy-governed precision
3. Explain: false positives cost customer trust and analyst time — Sentinel avoids them
4. Click **Close** → case closes with audit trail entry

---

## Demo Scenario 4 — Cross-Border Money Mule (CRITICAL)

**Story:** A newly opened account (12 days old) receives a $15,000 wire transfer, then immediately sends $14,800 to an overseas account in three rapid transfers to different recipients. Classic money mule pattern.

**What it tests:** NEW_ACCOUNT + AMOUNT_SPIKE + VELOCITY_CHECK_FAILED + ROUND_TRIP_TRANSFER pattern

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-DEMO-004",
    "customer_id": "CUST-NEW-7734",
    "customer_name": "Alex Petrov",
    "amount": 4933.33,
    "currency": "USD",
    "merchant": "International Wire Transfer",
    "merchant_category": "WIRE_TRANSFER",
    "channel": "ONLINE_BANKING",
    "country": "US",
    "city": "Miami",
    "destination_country": "UA",
    "device_id": "DEV-NEW-CHROME-01",
    "ip_address": "104.28.55.22",
    "timestamp": "2026-03-17T16:45:00Z",
    "customer_profile": {
      "account_age_days": 12,
      "home_country": "US",
      "avg_transaction_amount": 0,
      "prior_transaction_count": 2,
      "large_inbound_transfer_today": 15000.00,
      "outbound_today": 14800.00,
      "known_devices": []
    }
  }'
```

**Expected pipeline outcome:**
- Agent #1: `risk_score ≈ 95`, flags `AMOUNT_SPIKE`, `RARE_MCC`, `VELOCITY_CHECK_FAILED`
- Agent #2: `behavioral_score ≈ 97`, flags `NEW_ACCOUNT`, `NEW_DEVICE`, `BURST_ACTIVITY`, `GEO_DEVIATION`
- Agent #4: `final_risk_score ≈ 96`, severity `CRITICAL`
- Agent #5: executes `freeze_transaction` + `notify_security_team` + `escalate_to_human` + `create_case_report`

**What to show in the dashboard:**
1. Risk score 96 — highest in the demo
2. **4 actions executed** — show the actions tab in case detail
3. Show **Audit Timeline** — 6 events in under 2 minutes, all machine-executed
4. Assign to "Compliance Team" + note: "Potential money mule — cross-border AML review required"
5. Escalate → triggers senior analyst workflow

---

## Demo Scenario 5 — Stolen Card, Known Merchant (MEDIUM)

**Story:** Michael Thorne's card is used at a merchant he shops at regularly (Amazon), but from an unknown device and at 3:12 AM — outside all his historical purchase windows. Amount is normal. Pattern is subtle.

**What it tests:** That the system correctly weights behavioral signals over pattern signals — the merchant is known but time + device create suspicion.

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TX-DEMO-005",
    "customer_id": "CUST-MT04",
    "customer_name": "Michael Thorne",
    "amount": 189.99,
    "currency": "USD",
    "merchant": "Amazon",
    "merchant_category": "ONLINE_RETAIL",
    "channel": "CARD_NOT_PRESENT",
    "country": "US",
    "city": "Chicago",
    "device_id": "DEV-UNKNOWN-5583",
    "ip_address": "162.144.88.201",
    "timestamp": "2026-03-17T03:12:00Z",
    "customer_profile": {
      "home_country": "US",
      "home_city": "Chicago",
      "avg_transaction_amount": 220.00,
      "known_devices": ["DEV-MT-IPHONE-01", "DEV-MT-LAPTOP-01"],
      "known_ip_ranges": ["98.220.x.x"],
      "typical_active_hours": "08:00-22:00",
      "frequent_merchants": ["Amazon", "Target", "Costco"]
    }
  }'
```

**Expected pipeline outcome:**
- Agent #1: `risk_score ≈ 35`, flags `TIME_ANOMALY` (known merchant reduces pattern score)
- Agent #2: `behavioral_score ≈ 62`, flags `NEW_DEVICE`, `NEW_IP_RANGE`, `TIME_DEVIATION`
- Agent #4: `final_risk_score ≈ 52`, severity `MEDIUM` — behavioural signals outweigh pattern
- Agent #5: executes `notify_security_team` (no freeze — amount is normal, merchant is trusted)

**What to show in the dashboard:**
1. Severity `MEDIUM` — not a freeze, but a flag
2. Compare: Agent #1 score (35) vs Agent #2 score (62) — behavioural agent caught what pattern missed
3. This demonstrates **why multiple specialised agents outperform a single model**
4. Add note: "Possible stolen card — customer travel-time check recommended before close"

---

## The ADK Dev UI — Watch Agents Think in Real Time

While any of the above scenarios is running, open the ADK Dev UI at **http://localhost:8090**.

```
You will see:
  ├── SentinelOrchestrator receives the transaction JSON
  ├── → calls RunPatternAnalyzerTool
  │       ↳ TransactionPatternAnalyzer fires tools: AmountSpike, GeoDistance, Velocity...
  │       ↳ returns: { risk_score, flags[], reasoning }
  ├── → calls RunBehavioralRiskTool
  │       ↳ BehavioralRiskDetector fires: NewDevice, AmountDeviation, BurstActivity...
  │       ↳ returns: { behavioral_score, contributions }
  ├── → calls RunEvidenceBuilderTool
  │       ↳ EvidenceBuilderAgent: FlagMerger + SummaryComposer
  │       ↳ returns: { evidence_summary, combined_flags[] }
  ├── → calls RunAggregatedScorerTool
  │       ↳ AggregatedRiskScorer: WeightedBlend + SeverityClassifier
  │       ↳ returns: { final_risk_score, severity, recommended_action }
  └── → calls RunActionExecutorTool
          ↳ ActionExecutor: only policy-registered tools fire
          ↳ returns: { executed_actions[], audit_id }
```

Each agent's **reasoning**, **tool call inputs**, **tool call outputs**, and **final response** are visible in real time.

---

## Key Differentiators to Highlight to Judges

| Feature | What it shows |
|---------|--------------|
| **5 specialised agents** | Each has a single job — pattern, behaviour, evidence, scoring, action. No monolithic prompt doing everything. |
| **Google ADK compliance** | Strict tool registration. Agent #5 cannot take an action unless the tool is explicitly registered. No hallucinated actions. |
| **Explainability by design** | Agent #3 produces a human-readable evidence bundle *before* any action is taken. Every flag is traceable to a specific signal. |
| **False positive control** | Scenario 3 shows a legitimate $8,200 transaction correctly cleared. Over-triggering destroys customer trust. |
| **Behavioural AI** | Scenario 5 shows Agent #2 catching what Agent #1 missed — same merchant, same amount, different device+time. |
| **Policy-governed actions** | The same `recommended_action` from Agent #4 maps to different tool sets depending on severity. No improvisation. |
| **Full audit trail** | Every mutation (assign, note, escalate, close) appends a timestamped, correlation-ID-linked audit entry. |
| **Live deployment** | Running on Google Cloud Run right now. CI/CD via GitHub Actions. Not a prototype. |
| **ADK Dev UI** | You can watch the agents reason step-by-step at http://localhost:8090 — not a black box. |

---

## Architecture in One Sentence

> A raw transaction enters as JSON, passes through five specialised AI agents coordinated by an ADK Orchestrator, produces a calibrated risk score with a deterministic audit trail, and triggers only policy-approved actions — all visible in a live fraud analyst dashboard.

---

## Risk Score Reference

| Score | Severity | Typical Actions |
|-------|----------|----------------|
| 0–30 | LOW | `create_case_report` only |
| 31–60 | MEDIUM | `notify_security_team` |
| 61–80 | HIGH | `notify_security_team` + `request_step_up_auth` |
| 81–100 | CRITICAL | `freeze_transaction` + `notify_security_team` + `create_case_report` |

---

## Live Endpoints (Cloud Run)

```
API:       https://sentinel-api-113270635078.us-central1.run.app
Dashboard: https://sentinel-web-113270635078.us-central1.run.app

Quick health check:
  curl https://sentinel-api-113270635078.us-central1.run.app/health

Run a live pipeline:
  curl -X POST https://sentinel-api-113270635078.us-central1.run.app/api/ingest \
    -H "Content-Type: application/json" \
    -d @doc/agents/sample-transaction.json
```
