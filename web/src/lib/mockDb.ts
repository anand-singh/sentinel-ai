import {
  Alert,
  CaseDetail,
  Analytics,
} from '@/types/alert'

// ─── Mock Alerts ─────────────────────────────────────────────────────────────────
export const MOCK_ALERTS: Alert[] = [
  {
    id: 'ALERT-001',
    transactionId: 'TX-9204',
    customerId: 'CUST-SJ01',
    customerName: 'Sarah Jenkins',
    finalRiskScore: 85,
    severity: 'CRITICAL',
    status: 'REVIEWING',
    recommendedAction: 'freeze_transaction',
    timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
  },
  {
    id: 'ALERT-002',
    transactionId: 'TX-9198',
    customerId: 'CUST-RC02',
    customerName: 'Robert Chen',
    finalRiskScore: 58,
    severity: 'MEDIUM',
    status: 'QUEUED',
    recommendedAction: 'notify_security_team',
    timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
  },
  {
    id: 'ALERT-003',
    transactionId: 'TX-9195',
    customerId: 'CUST-EW03',
    customerName: 'Emily Watson',
    finalRiskScore: 28,
    severity: 'LOW',
    status: 'RESOLVED',
    recommendedAction: 'create_case_report',
    timestamp: new Date(Date.now() - 42 * 60 * 1000).toISOString(),
  },
  {
    id: 'ALERT-004',
    transactionId: 'TX-9192',
    customerId: 'CUST-MT04',
    customerName: 'Michael Thorne',
    finalRiskScore: 91,
    severity: 'CRITICAL',
    status: 'REVIEWING',
    recommendedAction: 'freeze_transaction',
    timestamp: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'ALERT-005',
    transactionId: 'TX-9188',
    customerId: 'CUST-AL05',
    customerName: 'Alice Leung',
    finalRiskScore: 72,
    severity: 'HIGH',
    status: 'QUEUED',
    recommendedAction: 'notify_security_team',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 'ALERT-006',
    transactionId: 'TX-9180',
    customerId: 'CUST-DR06',
    customerName: 'David Ross',
    finalRiskScore: 38,
    severity: 'LOW',
    status: 'CLOSED',
    recommendedAction: 'create_case_report',
    timestamp: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
  },
]

// ─── Mock Cases ──────────────────────────────────────────────────────────────────
export const MOCK_CASES: CaseDetail[] = [
  {
    caseId: 'F-9204',
    alert: MOCK_ALERTS[0],
    status: 'IN_REVIEW',
    assignedTo: 'Marcus Vance',
    policyVersion: 'v3.1.2',
    agentVersion: 'sentinel-agent-v2.4.1',
    notes: ['Initial review started. Transaction from Lagos vs history of New York.'],
    relatedTransactionIds: ['TX-9204', 'TX-9201', 'TX-9199'],
    agentOutputs: [
      {
        agentName: 'Pattern Analyzer',
        summary: 'Transaction pattern shows rapid geographic shift — login from Lagos, NG after history of New York, US logins.',
        flags: ['GEO_MISMATCH', 'RAPID_LOCATION_CHANGE'],
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Behavioral Risk Agent',
        summary: 'Amount ($4,950.00) is 340% above this customer\'s 30-day average. New device fingerprint detected.',
        flags: ['AMOUNT_SPIKE', 'NEW_DEVICE'],
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Evidence Builder',
        summary: '4 transactions in under 30 seconds (velocity check failed). Transaction to previously-unseen merchant.',
        flags: ['VELOCITY_CHECK_FAILED', 'NEW_MERCHANT'],
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Aggregated Risk Scorer',
        summary: 'Combined signal weight produces a final risk score of 85/100 — classified as VERY HIGH.',
        flags: ['HIGH_RISK_COMPOSITE'],
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
      },
    ],
    actionsExecuted: [
      {
        id: 'ACT-001',
        type: 'freeze_transaction',
        performedBy: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
      },
      {
        id: 'ACT-002',
        type: 'notify_security_team',
        performedBy: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 1.5 * 60 * 1000).toISOString(),
        note: 'Security team notified via PagerDuty.',
      },
      {
        id: 'ACT-003',
        type: 'create_case_report',
        performedBy: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 1 * 60 * 1000).toISOString(),
      },
    ],
    auditTrail: [
      {
        id: 'AUD-001',
        event: 'Transaction flagged by Pattern Analyzer',
        actor: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
        correlationId: 'CORR-TX9204-001',
        policyVersion: 'v3.1.2',
      },
      {
        id: 'AUD-002',
        event: 'Risk score computed: 85 (VERY HIGH)',
        actor: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 2.5 * 60 * 1000).toISOString(),
        correlationId: 'CORR-TX9204-002',
      },
      {
        id: 'AUD-003',
        event: 'Transaction frozen',
        actor: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
        correlationId: 'CORR-TX9204-003',
        policyVersion: 'v3.1.2',
      },
      {
        id: 'AUD-004',
        event: 'Case assigned to Marcus Vance',
        actor: 'system',
        timestamp: new Date(Date.now() - 1 * 60 * 1000).toISOString(),
        correlationId: 'CORR-TX9204-004',
      },
    ],
  },
  {
    caseId: 'F-9198',
    alert: MOCK_ALERTS[1],
    status: 'OPEN',
    assignedTo: null,
    policyVersion: 'v3.1.2',
    agentVersion: 'sentinel-agent-v2.4.1',
    notes: [],
    relatedTransactionIds: ['TX-9198'],
    agentOutputs: [
      {
        agentName: 'Pattern Analyzer',
        summary: 'Medium-risk transaction. Slightly elevated amount, known merchant.',
        flags: ['AMOUNT_ELEVATED'],
        timestamp: new Date(Date.now() - 16 * 60 * 1000).toISOString(),
      },
    ],
    actionsExecuted: [
      {
        id: 'ACT-010',
        type: 'notify_security_team',
        performedBy: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
      },
    ],
    auditTrail: [
      {
        id: 'AUD-010',
        event: 'Transaction flagged',
        actor: 'sentinel-agent-v2.4.1',
        timestamp: new Date(Date.now() - 16 * 60 * 1000).toISOString(),
        correlationId: 'CORR-TX9198-001',
      },
    ],
  },
]

// ─── Mock Analytics ───────────────────────────────────────────────────────────────
export const MOCK_ANALYTICS: Analytics = {
  alertsBySeverity: [
    { severity: 'CRITICAL', count: 2 },
    { severity: 'HIGH', count: 1 },
    { severity: 'MEDIUM', count: 1 },
    { severity: 'LOW', count: 2 },
  ],
  trendsLast30Days: Array.from({ length: 30 }, (_, i) => ({
    date: new Date(Date.now() - (29 - i) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    count: Math.floor(Math.random() * 60) + 20,
  })),
  topFlags: [
    { flag: 'GEO_MISMATCH', count: 43 },
    { flag: 'AMOUNT_SPIKE', count: 38 },
    { flag: 'NEW_DEVICE', count: 31 },
    { flag: 'VELOCITY_CHECK_FAILED', count: 27 },
    { flag: 'NEW_MERCHANT', count: 25 },
  ],
  avgTimeToCloseHours: 3.4,
  escalationRatePct: 12,
}

// ─── In-memory mutable state (simulates a live DB) ───────────────────────────────
export const db = {
  alerts: [...MOCK_ALERTS],
  cases: [...MOCK_CASES],
  analytics: { ...MOCK_ANALYTICS },
}
