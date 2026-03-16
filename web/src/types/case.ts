export type RiskLevel = 'low' | 'medium' | 'high' | 'critical'
export type CaseStatus = 'open' | 'in_review' | 'closed' | 'escalated'

export interface Transaction {
  id: string
  date: string
  amount: number
  merchant: string
  status: 'completed' | 'flagged' | 'denied'
}

export interface LinkedEntity {
  id: string
  type: 'account' | 'device' | 'ip'
  relationship: string
  riskLevel: RiskLevel
}

export interface Case {
  id: string
  customerName: string
  amount: number
  date: string
  riskLevel: RiskLevel
  status: CaseStatus
  description: string
  transactions?: Transaction[]
  linkedEntities?: LinkedEntity[]
}
