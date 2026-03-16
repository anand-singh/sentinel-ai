
# 🛡️ Sentinel AI (Agent ThinkING)

**Sentinel** is a Java/Maven, cloud‑ready, multi‑agent fraud defense platform for banking.  
It ingests transactions in real time, analyzes patterns and behavior, explains risk, aggregates scores, and executes **safe, policy‑governed actions**—all visible in a **Fraud Case Management Dashboard**.

> In simple words: **your 24/7 digital fraud‑fighting team**.

---

## ✨ Core Capabilities

- **Multi‑Agent Architecture (Java services)**
    1) **Transaction Pattern Analyzer** – spends/location/merchant anomalies
    2) **Behavioral Risk Agent** – customer‑specific deviation scoring
    3) **Evidence Builder** – deterministic, audit‑ready explanation bundles
    4) **Aggregated Risk Scorer** – 0–100 score + severity + recommendation
    5) **Action Executor (ADK‑style)** – freeze/notify/step‑up auth via explicit tools

- **Fraud Case Management Dashboard**
    - Alerts, agent outputs, risk contributions, actions, notes
    - **Interactive timeline** + related‑activity graph
    - Case lifecycle (OPEN → REVIEW → ESCALATED → CLOSED)

- **Google Cloud + Google AI**
    - **Vertex AI** (Gemini) for optional reasoning and modeling
    - **ADK design patterns** for safe tool‑calling & governance
    - **Pub/Sub** (ingest), **Dataflow** (ETL), **BigQuery** (analytics)
    - **Cloud Run** (services), **Firestore/AlloyDB** (cases/profiles)
    - **Cloud Logging/Monitoring**, **IAM** (security, audit)

---

