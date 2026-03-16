# Sentinel AI - Web

Fraud Case Management Dashboard for the Sentinel AI platform.

---

## ✨ Features

- **Alerts & Case Management** – View agent outputs, risk contributions, and actions
- **Interactive Timeline** – Track case activity and related events
- **Case Lifecycle** – Manage cases through OPEN → REVIEW → ESCALATED → CLOSED
- **Notes & Collaboration** – Add investigation notes and team comments

---

## 🚀 Getting Started (Development)

### Prerequisites

| Requirement | Version | Notes                  |
|-------------|---------|------------------------|
| **Node.js** | 18+     | Runtime                |
| **npm**     | 9+      | Package manager        |

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

---

## 🐛 Troubleshooting

| Issue                  | Solution                              |
|------------------------|---------------------------------------|
| Port already in use    | Change port: `npm run dev -- -p 3001` |
| Dependencies not found | Run `npm install`                     |
