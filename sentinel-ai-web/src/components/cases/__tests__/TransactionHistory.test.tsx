import { render, screen } from '@testing-library/react'
import { TransactionHistory } from '../TransactionHistory'
import { Transaction } from '@/types/case'

const mockTransactions: Transaction[] = [
  { id: 'TX-101', date: '2026-03-15T10:00:00Z', amount: 500, merchant: 'Amazon', status: 'completed' },
  { id: 'TX-102', date: '2026-03-15T10:05:00Z', amount: 750, merchant: 'Best Buy', status: 'flagged' },
]

describe('TransactionHistory', () => {
  it('renders a list of transactions', () => {
    render(<TransactionHistory transactions={mockTransactions} />)
    expect(screen.getByText(/TX-101/i)).toBeInTheDocument()
    expect(screen.getByText(/Amazon/i)).toBeInTheDocument()
    expect(screen.getByText(/flagged/i)).toBeInTheDocument()
  })
})
