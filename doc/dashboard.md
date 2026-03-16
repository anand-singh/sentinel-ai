# 🛡️ Fraud Case Management Dashboard (Sentinel / Agent ThinkING)

The **Fraud Case Management Dashboard** is the central nerve center of **Sentinel**.  
It enables fraud analysts to review **alerts, flags, risk scores, agent explanations, actions, timelines, related transactions, and full case details** in one place.

> In simple words: a fast, clear, auditable workspace for fraud investigation.

---

## ✨ What Analysts Can Do

- View real‑time **fraud alerts**
- Inspect **flags, risk scores**, and **agent explanations**
- See **actions taken** (freeze, notify, create case, etc.)
- Follow the **timeline of events** with correlation IDs
- Explore **related transactions** (same device/IP/merchant cluster)
- Open, assign, comment, escalate, and close **cases**
- Access a full **audit trail** and read‑only **policy/version** info

---

## ✅ STEP 1 — Goals of the Dashboard

1) **View incoming fraud alerts**  
Every alert shows:
- `Transaction ID`, `Customer ID`
- **Final Risk Score** (from Aggregated Risk Scorer — Agent #4)
- **Severity** (LOW / MED / HIGH / CRITICAL)
- **Recommended Action**
- **Timestamp**

2) **View entire agent reasoning in one place**
- Pattern Analyzer output
- Behavioral Risk output
- Evidence Builder summary
- AML Agent signals *(future)*

3) **View executed actions** *(from Action Executor — Agent #5)*
- `freeze_transaction`
- `notify_security_team`
- `create_case_report`
- etc.

4) **Open, assign, and track cases**
- `Case ID`, `Owner`, `Notes`
- `Status`: OPEN / IN REVIEW / ESCALATED / CLOSED

5) **Full audit trail**
- Every agent decision
- Every tool executed
- Policy versions
- Timing & correlation IDs

6) **Relationship graph** *(optional but impactful)*
- Visual graph of related suspicious transactions
- Links by device, IP, merchant, and other features

---

## ✅ STEP 2 — Dashboard Pages

### 🖥️ Page 1 — Alerts Overview (Home)
- Live table or card list
- Filters: **severity, date, customer, merchant**
- Search bar
- Real‑time updates via **Pub/Sub push** or **polling**

### 🖼️ Page 2 — Case Detail View
Shows everything for one transaction/case:
- Final score, severity, explanation
- All agent outputs (tabs)
- Graph of related activity
- Audit trail (interactive timeline)
- Actions taken
- Quick actions: **Assign**, **Add note**, **Escalate**, **Close case**

### 📊 Page 3 — Analytics Dashboard
- Count of alerts per severity
- Trends over time
- Top root‑cause flags
- Common fraud patterns

### 🛠️ Page 4 — Admin / Policy Viewer
- Read‑only view of configurations
- Agent versions & policy versions
- Alert routing behavior

---

## ✅ STEP 3 — Backend APIs (Draft)

> Expose from Cloud Run services or a dedicated API gateway.

```

GET  /alerts                      # List alerts with pagination
GET  /cases/:id                   # Full case detail (agent outputs, evidence, audit logs)
POST /cases/:id/assign            # Assign to a user
POST /cases/:id/note              # Add analyst note
POST /cases/:id/escalate          # Mark high priority
POST /cases/:id/close             # Close case
GET  /analytics                   # Aggregated insights for dashboards

````

---

## ✅ STEP 4 — Frontend Stack

Choose one stack (recommended first):

- **React + TypeScript + Material UI** ✅
- Vue 3 + Tailwind
- Angular
- SvelteKit

**Key UI components**
- Alert list table
- Risk score indicator
- Severity chips (color coded)
- Agent explanation viewer (tabbed)
- Audit log viewer (**interactive timeline**)
- Relationship graph (D3.js / Recharts)

---

## ✅ STEP 5 — Data Sources & Event Flow

**From upstream agents**
- Pattern Agent → explanation
- Behavioral Agent → behavioral context
- Evidence Builder → consolidated summary
- Aggregated Risk Agent → **final score**
- Action Executor Agent → **executed actions & audit**

**Data storage**
- **BigQuery** → historical events & analytics
- **Firestore / AlloyDB** → active cases, notes, assignments
- **Cloud Logging** → immutable audit logs

---

## ✅ STEP 6 — Security & Access Control

- **IAM‑secured API** access
- Logged‑in analyst identity (OIDC/JWT)
- **RBAC** roles:
  - `Viewer` — read‑only
  - `Analyst` — update notes, assign, escalate
  - `Supervisor` — policy viewers, closures, advanced ops
- Every action writes an **audit log entry**

---

## ✅ STEP 7 — CI/CD & Deployment

- Build frontend → deploy to **Firebase Hosting** or **Cloud Run**
- Backend services run on **Cloud Run**
- **Cloud Build / GitHub Actions** → automate build/test/deploy

---

## 🗺️ Example Screens & Diagrams

> Include images/screenshots in `/docs/images` and reference them here.

- Alerts Overview (screenshot)
- Case Detail with **interactive timeline**
- Relationship graph
- Architecture & sequence diagrams (Mermaid)

```mermaid
sequenceDiagram
  autonumber
  participant UI as Dashboard UI
  participant API as Case API
  participant Store as Case Store (Firestore/AlloyDB)
  participant BQ as BigQuery (Analytics)
  note over UI: User opens Alerts
  UI->>API: GET /alerts?filters...
  API-->>UI: Alerts list (paginated)

  note over UI: User opens Case Detail
  UI->>API: GET /cases/:id
  API->>Store: fetch case + notes + actions
  API->>BQ: fetch aggregated analytics (optional)
  Store-->>API: case payload
  BQ-->>API: analytics payload
  API-->>UI: case detail + evidence + timeline + related graph
````

***

## 🧱 Suggested Repo Structure

    /dashboard
      /src
        /components
          AlertsTable.tsx
          RiskScore.tsx
          SeverityChip.tsx
          AgentTabs.tsx
          EvidencePanel.tsx
          Timeline.tsx
          RelatedGraph.tsx
        /pages
          Alerts.tsx
          CaseDetail.tsx
          Analytics.tsx
          Admin.tsx
        /api
          alerts.ts
          cases.ts
          analytics.ts
        /state
          store.ts
        /styles
      /public
      package.json
    /backend
      /mock-server (optional for local dev)
      /api-gateway (if used)
    /docs
      /images
      /diagrams

***

## 🧪 Local Development (Example)

```bash
# Frontend
cd dashboard
npm i
npm run dev

# Optional mock API (Node/TS)
cd backend/mock-server
npm i
npm run dev
```

***

## 🔌 Environment Variables (UI)

Create `.env` in `/dashboard`:

```env
VITE_API_BASE_URL=http://localhost:8081
VITE_AUTH_ISSUER=https://accounts.google.com
VITE_ALLOWED_AUDIENCE=sentinel-dashboard
```

***

## 📊 Analytics Cards (Examples)

*   Alerts by severity (bar/stacked)
*   Mean/median risk score by day
*   Top flags (AMOUNT\_SPIKE, GEO\_MISMATCH, NEW\_DEVICE…)
*   Time‑to‑close per severity
*   Escalation rate over time

***

## 🧩 Accessibility & UX

*   Keyboard‑navigable tables & timelines
*   Visible focus states
*   High‑contrast mode / dark mode (optional)
*   Toasts and inline validation messages

***

## 🔒 Compliance Notes

*   Do not display raw PII unless required; mask where possible
*   Include **policy versions** and **agent versions** in case detail
*   Preserve **immutable audit logs** for all actions
*   Respect data retention and deletion policies

***

## 🗺️ Roadmap

*   Advanced forensic **timeline** (multi‑column by entity: device, IP, merchant)
*   “Playback mode” for event sequences
*   Case SLA & queue management
*   AML agent integration + sanctions signals
*   Analyst productivity features (keyboard actions, saved filters)

***

## 🤝 Contributing

*   Open an issue for feature requests/bugs
*   Follow code style (ESLint + Prettier)
*   Add tests for components & API wrappers
*   Keep docs and diagrams updated

***

## 📜 License

Specify your license (MIT/Apache‑2.0/Proprietary) in `LICENSE`.
