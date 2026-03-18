import {
  Alert,
  CaseDetail,
  Analytics,
} from '@/types/alert'

// ─── Mock Alerts ─────────────────────────────────────────────────────────────────
export const MOCK_ALERTS: Alert[] = [
  {
    id: 'ALERT-001',
    transactionId: 'TX-DEMO-001',
    customerId: 'CUST-SJ01',
    customerName: 'Sarah Jenkins',
    finalRiskScore: 95,
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
    policyVersion: 'action-policy-2026-03-16',
    agentVersion: 'orchestrator-v1.0.0',
    notes: ['Initial review started. Transaction from Lagos, NG vs home location New York, US. Impossible travel window confirmed (4 min, 8,473 km).'],
    relatedTransactionIds: ['TX-DEMO-001', 'TX-9201', 'TX-9199'],
    agentOutputs: [
      {
        agentName: 'Pattern Analyzer',
        summary: 'Anomalies detected: unusual amount (+34.6 pts), impossible travel (+25.0 pts). Amount z-score 4.45 — transaction is $4,950 vs global avg $500. Geo distance 8,472.7 km in 4 minutes (127,090 km/h — physically impossible). Total risk: 70/100.',
        flags: ['AMOUNT_SPIKE', 'GEO_MISMATCH'],
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Behavioral Risk Agent',
        summary: 'Amount 96.1σ above customer\'s normal ($145 avg). Travel 8,473 km from last known location (New York) in 4 minutes — impossible. New device fingerprint DEV-NEW-9921 not in known_devices. IP 197.211.62.10 is Nigerian range, not US range. Unusual merchant category. Burst: 5 txns/hr vs baseline 0.1. New account (5 days old) — high money mule risk.',
        flags: ['AMOUNT_DEVIATION', 'GEO_DEVIATION', 'NEW_DEVICE', 'NEW_IP_RANGE', 'RARE_MERCHANT', 'BURST_ACTIVITY', 'NEW_ACCOUNT'],
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Evidence Builder',
        summary: 'High amount vs pattern, abnormal location. New device, abnormal location distance (8,473 km), new IP range (NG), burst activity (50× baseline). 9 combined flags across pattern and behavioral agents.',
        flags: ['AMOUNT_SPIKE', 'GEO_MISMATCH', 'NEW_DEVICE', 'BURST_ACTIVITY', 'GEO_DEVIATION', 'NEW_IP_RANGE', 'AMOUNT_DEVIATION', 'NEW_ACCOUNT', 'RARE_MERCHANT'],
        timestamp: new Date(Date.now() - 2.5 * 60 * 1000).toISOString(),
      },
      {
        agentName: 'Aggregated Risk Scorer',
        summary: 'Blended score: pattern 0.7 × 0.5 + behavioral 1.0 × 0.5 = 0.85. Boost +0.10 applied (Amount+Device+Geo; 3× new signals). Calibrated: 0.95 → 95. Severity: CRITICAL. Action: BLOCK. Source contributions: pattern_agent=35, behavioral_agent=50.',
        flags: ['AMOUNT_SPIKE', 'GEO_MISMATCH', 'NEW_DEVICE', 'BURST_ACTIVITY', 'GEO_DEVIATION', 'NEW_IP_RANGE', 'AMOUNT_DEVIATION', 'NEW_ACCOUNT', 'RARE_MERCHANT'],
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
      },
    ],
    actionsExecuted: [
      {
        id: 'ACT-001',
        type: 'freeze_transaction',
        performedBy: 'orchestrator-v1.0.0',
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
      },
      {
        id: 'ACT-002',
        type: 'notify_security_team',
        performedBy: 'orchestrator-v1.0.0',
        timestamp: new Date(Date.now() - 1.5 * 60 * 1000).toISOString(),
        note: 'Security team notified via SOC — CRITICAL: Critical risk detected, transaction blocked.',
      },
      {
        id: 'ACT-003',
        type: 'create_case_report',
        performedBy: 'orchestrator-v1.0.0',
        timestamp: new Date(Date.now() - 1 * 60 * 1000).toISOString(),
      },
    ],
    auditTrail: [
      {
        id: 'AUD-001',
        event: 'TransactionPatternAnalyzer — COMPLETE',
        actor: 'sentinel-orchestrator',
        timestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-001',
        policyVersion: 'action-policy-2026-03-16',
      },
      {
        id: 'AUD-002',
        event: 'BehavioralRiskDetector — COMPLETE',
        actor: 'sentinel-orchestrator',
        timestamp: new Date(Date.now() - 4 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-002',
        policyVersion: 'action-policy-2026-03-16',
      },
      {
        id: 'AUD-003',
        event: 'EvidenceBuilderAgent — COMPLETE',
        actor: 'sentinel-orchestrator',
        timestamp: new Date(Date.now() - 3 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-003',
        policyVersion: 'action-policy-2026-03-16',
      },
      {
        id: 'AUD-004',
        event: 'AggregatedRiskScorer — COMPLETE (score=95, CRITICAL, BLOCK)',
        actor: 'sentinel-orchestrator',
        timestamp: new Date(Date.now() - 2.5 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-004',
        policyVersion: 'action-policy-2026-03-16',
      },
      {
        id: 'AUD-005',
        event: 'ActionExecutor — COMPLETE (freeze_transaction, notify_security_team, create_case_report)',
        actor: 'sentinel-orchestrator',
        timestamp: new Date(Date.now() - 2 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-005',
        policyVersion: 'action-policy-2026-03-16',
      },
      {
        id: 'AUD-006',
        event: 'Case assigned to Marcus Vance',
        actor: 'system',
        timestamp: new Date(Date.now() - 1 * 60 * 1000).toISOString(),
        correlationId: 'TX-DEMO-001-20260317090400-006',
      }
    ]
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
