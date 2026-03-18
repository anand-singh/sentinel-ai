import { render, screen } from '@testing-library/react'
import { CaseTable } from '../CaseTable'
import { Case } from '@/types/case'

const mockCases: Case[] = [
  {
    id: 'CASE-001',
    customerName: 'John Doe',
    amount: 1250,
    date: '2026-03-15T10:30:00Z',
    riskLevel: 'high',
    status: 'open',
    description: 'Test case'
  }
]

describe('CaseTable', () => {
  it('renders a list of cases', () => {
    render(<CaseTable cases={mockCases} />)
    expect(screen.getByText(/CASE-001/i)).toBeInTheDocument()
    expect(screen.getByText(/John Doe/i)).toBeInTheDocument()
    expect(screen.getByText(/high/i)).toBeInTheDocument()
  })
})
