# Track Plan: Fraud Case Management Dashboard

## Phase 1: Project Initialization & Structure

- [ ] **Task 1: Scaffold Frontend Application**
  - [ ] Initialize project using React + TypeScript + Vite
  - [ ] Install Material UI and configure the base theme/colors
  - [ ] Set up directory structure (`/components`, `/pages`, `/api`, `/state`)
- [ ] **Task 2: Configure Environment & Tooling**
  - [ ] Add ESLint and Prettier configurations (enforce code style)
  - [ ] Create `.env` template with `VITE_API_BASE_URL` and `VITE_AUTH_ISSUER`
- [ ] **Task 3: Setup Application Routing**
  - [ ] Install and configure React Router
  - [ ] Create skeleton routes for `/`, `/cases/:id`, `/analytics`, and `/admin`

## Phase 2: Reusable UI Components

- [ ] **Task 1: Build Status & Metric Components**
  - [ ] Create `SeverityChip.tsx` (color-coded by LOW/MED/HIGH/CRITICAL)
  - [ ] Create `RiskScore.tsx` (visual indicator of final risk score)
- [ ] **Task 2: Build Case Detail Components**
  - [ ] Create `AgentTabs.tsx` to display different agent explanations
  - [ ] Create `EvidencePanel.tsx` for summary context
- [ ] **Task 3: Build Advanced Visual Components**
  - [ ] Create `Timeline.tsx` for interactive audit logs
  - [ ] Create `RelatedGraph.tsx` placeholder for D3.js/Recharts transaction mapping

## Phase 3: Core Pages Construction

- [ ] **Task 1: Build Alerts Overview Page (Home)**
  - [ ] Create `AlertsTable.tsx` for incoming alerts
  - [ ] Add filters for severity, date, customer, and merchant
  - [ ] Implement search bar functionality
- [ ] **Task 2: Build Case Detail Page**
  - [ ] Implement layout containing Risk Score, Severity, and Agent Tabs
  - [ ] Embed the Interactive Timeline for audits
  - [ ] Add quick-action buttons (Assign, Note, Escalate, Close)
- [ ] **Task 3: Build Analytics & Admin Pages**
  - [ ] Create `Analytics.tsx` with charts (e.g., Alerts by severity, top flags)
  - [ ] Create read-only `Admin.tsx` for viewing policy config and agent versions

## Phase 4: Data Layer & API Integration

- [ ] **Task 1: Setup Local Mock Server (Optional but recommended)**
  - [ ] Create simple Node/TS mock server to simulate `/alerts`, `/cases/:id`, etc.
- [ ] **Task 2: Implement API Services (`/api` folder)**
  - [ ] Write `alerts.ts` (fetch/pagination)
  - [ ] Write `cases.ts` (fetch details, POST endpoints for actions)
  - [ ] Write `analytics.ts`
- [ ] **Task 3: Global State & Data Fetching**
  - [ ] Set up state management (`store.ts`) or React Query for fetching and caching
  - [ ] Hook up API endpoints to the physical UI components

## Phase 5: Polish & Security

- [ ] **Task 1: Security Implementation**
  - [ ] Add IAM/OIDC token passing to API requests
  - [ ] Apply PII UI-masking logic across vulnerable data fields
- [ ] **Task 2: UX and Accessibility Review**
  - [ ] Verify keyboard navigation on tables and timelines
  - [ ] Add toasts/inline notifications for fast feedback on Analyst actions (e.g., "Case Closed successfully")
