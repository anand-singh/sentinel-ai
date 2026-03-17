# Sentinel AI - Web

Fraud Case Management Dashboard — the central nerve center of Sentinel.

---

## ✨ Features

The dashboard allows fraud analysts to:

- **View Alerts** – Transaction ID, Customer ID, Risk Score, Severity, Recommended Action
- **Review Agent Reasoning** – Pattern Analyzer, Behavioral Risk, Evidence Builder outputs
- **Track Actions** – Freeze transactions, notify security, create case reports
- **Manage Cases** – Open, assign, escalate, and close cases with notes
- **Audit Trail** – Every agent decision, tool execution, policy version, and timing
- **Relationship Graph** – Visual graph of related suspicious transactions

---

## 📄 Dashboard Pages

| Page | Description |
|------|-------------|
| **Alerts Overview** | Live table with filters (severity, date, customer), search, real-time updates |
| **Case Detail** | Full case view: score, severity, agent outputs, graph, audit trail, actions |
| **Analytics** | Alert counts, trends, top flags, common fraud patterns |
| **Admin / Policy Viewer** | Read-only view of agent versions, policies, alert routing |

---

## 🔌 Backend APIs

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/alerts` | GET | List alerts with pagination |
| `/cases/:id` | GET | Full case detail (agent outputs, evidence, audit logs) |
| `/cases/:id/assign` | POST | Assign case to analyst |
| `/cases/:id/note` | POST | Add analyst note |
| `/cases/:id/escalate` | POST | Flag as high priority |
| `/cases/:id/close` | POST | Close the case |
| `/analytics` | GET | Aggregated insights |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React + TypeScript + Material UI |
| **Visualization** | D3.js / Recharts (relationship graphs) |
| **Hosting** | Firebase Hosting or Cloud Run |
| **CI/CD** | Cloud Build |

### Key Components

- Alert list table
- Risk score indicator
- Severity chips (color-coded)
- Agent explanation viewer
- Audit log viewer
- Relationship graph

---

## 📊 Data Sources

| Source | Purpose |
|--------|---------|
| **Pattern Agent** | Transaction anomaly explanations |
| **Behavioral Agent** | Customer deviation context |
| **Evidence Builder** | Audit-ready summaries |
| **Aggregated Risk Agent** | Final 0-100 score |
| **Action Executor** | Executed actions log |

### Storage

- **BigQuery** – Historical events
- **Firestore/AlloyDB** – Active cases
- **Cloud Logging** – Audit logs

---

## 🔐 Security

- IAM-based secure API
- Analyst identity authentication
- Role-based access control:
  - `Viewer` – Read-only access
  - `Analyst` – Case management
  - `Supervisor` – Full access + admin
- All actions produce audit logs

---

## 🚀 Getting Started (Development)

### Prerequisites

| Requirement | Version | Notes           |
|-------------|---------|-----------------|
| **Node.js** | 18+     | Runtime         |
| **npm**     | 9+      | Package manager |

### Install & Run

```bash
cd sentinel-ai/web

# Install dependencies
npm install

# Start development server
npm run dev
```

The dashboard starts on `http://localhost:3000` by default.

### Build for Production

```bash
npm run build
npm run start
```

### Deploy

```bash
# Deploy to Firebase Hosting
firebase deploy --only hosting

# Or deploy to Cloud Run
gcloud run deploy sentinel-web --source .
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port already in use | Change port: `npm run dev -- -p 3001` |
| Dependencies not found | Run `npm install` |
| API connection failed | Verify backend is running and CORS is configured |
