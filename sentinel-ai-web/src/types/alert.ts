// ─── Shared Enums ───────────────────────────────────────────────────────────────
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type AlertStatus = 'REVIEWING' | 'QUEUED' | 'RESOLVED' | 'CLOSED'
export type CaseStatus = 'OPEN' | 'IN_REVIEW' | 'ESCALATED' | 'CLOSED'
export type ActionType =
  | 'freeze_transaction'
  | 'notify_security_team'
  | 'create_case_report'
  | 'assign'
  | 'note'
  | 'escalate'

// ─── Agent Outputs ───────────────────────────────────────────────────────────────
export interface AgentOutput {
  agentName: string
  summary: string
  flags: string[]
  timestamp: string
}

// ─── Action Timeline Entry ───────────────────────────────────────────────────────
export interface ActionEntry {
  id: string
  type: ActionType
  performedBy: string
  timestamp: string
  note?: string
}

// ─── Audit Log Entry ─────────────────────────────────────────────────────────────
export interface AuditEntry {
  id: string
  event: string
  actor: string
  timestamp: string
  correlationId: string
  policyVersion?: string
}

// ─── Alert (list item) ───────────────────────────────────────────────────────────
export interface Alert {
  id: string
  transactionId: string
  customerId: string
  customerName: string
  finalRiskScore: number
  severity: Severity
  status: AlertStatus
  recommendedAction: string
  timestamp: string
  caseId?: string
}

// ─── Full Case Detail ─────────────────────────────────────────────────────────────
export interface CaseDetail {
  caseId: string
  alert: Alert
  status: CaseStatus
  assignedTo: string | null
  agentOutputs: AgentOutput[]
  actionsExecuted: ActionEntry[]
  auditTrail: AuditEntry[]
  relatedTransactionIds: string[]
  notes: string[]
  policyVersion: string
  agentVersion: string
}

// ─── Analytics ───────────────────────────────────────────────────────────────────
export interface AnalyticsSeverityCount {
  severity: Severity
  count: number
}

export interface AnalyticsDailyTrend {
  date: string
  count: number
}

export interface Analytics {
  alertsBySeverity: AnalyticsSeverityCount[]
  trendsLast30Days: AnalyticsDailyTrend[]
  topFlags: { flag: string; count: number }[]
  avgTimeToCloseHours: number
  escalationRatePct: number
}
