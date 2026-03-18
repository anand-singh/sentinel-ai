import { Case } from '@/types/case'

const MOCK_CASES: Case[] = [
  {
    id: 'CASE-001',
    customerName: 'John Doe',
    amount: 1250.00,
    date: '2026-03-15T10:30:00Z',
    riskLevel: 'high',
    status: 'open',
    description: 'Suspicious large transaction from new device',
    transactions: [
      { id: 'TX-1001', date: '2026-03-15T10:25:00Z', amount: 1250.00, merchant: 'Electronics Emporium', status: 'flagged' },
      { id: 'TX-1000', date: '2026-03-15T08:10:00Z', amount: 45.20, merchant: 'Coffee Shop', status: 'completed' }
    ],
    linkedEntities: [
      { id: 'ACC-5012', type: 'account', relationship: 'Same Phone Number', riskLevel: 'high' },
      { id: 'IP-192.168.1.45', type: 'ip', relationship: 'Matching Login IP', riskLevel: 'medium' }
    ]
  },
  {
    id: 'CASE-002',
    customerName: 'Jane Smith',
    amount: 450.50,
    date: '2026-03-16T09:15:00Z',
    riskLevel: 'medium',
    status: 'in_review',
    description: 'Multiple failed login attempts followed by transaction',
    transactions: [
      { id: 'TX-2001', date: '2026-03-16T09:10:00Z', amount: 450.50, merchant: 'Online Retailer', status: 'flagged' }
    ],
    linkedEntities: [
      { id: 'DEV-IPHONE-12', type: 'device', relationship: 'Device Fingerprint Match', riskLevel: 'low' }
    ]
  },
  {
    id: 'CASE-003',
    customerName: 'Alice Johnson',
    amount: 5000.00,
    date: '2026-03-14T14:20:00Z',
    riskLevel: 'critical',
    status: 'escalated',
    description: 'Possible account takeover; high-value wire transfer',
    transactions: [
      { id: 'TX-3001', date: '2026-03-14T14:15:00Z', amount: 5000.00, merchant: 'Global Wire Transfer', status: 'flagged' },
      { id: 'TX-3000', date: '2026-03-14T12:00:00Z', amount: 10.00, merchant: 'Test Merchant', status: 'completed' }
    ],
    linkedEntities: [
      { id: 'ACC-9988', type: 'account', relationship: 'Beneficiary of Wire Transfer', riskLevel: 'critical' },
      { id: 'IP-45.67.12.3', type: 'ip', relationship: 'Tor Exit Node', riskLevel: 'high' }
    ]
  }
]

export async function getCases(): Promise<Case[]> {
  // Simulate API delay
  return new Promise((resolve) => {
    setTimeout(() => resolve(MOCK_CASES), 100)
  })
}

export async function getCase(id: string): Promise<Case | undefined> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(MOCK_CASES.find(c => c.id === id)), 100)
  })
}
