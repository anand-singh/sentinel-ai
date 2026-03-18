import { render, screen, fireEvent } from '@testing-library/react'
import { CaseList } from '../CaseList'
import { Case } from '@/types/case'

const mockCases: Case[] = [
  { id: 'CASE-001', customerName: 'Alice', riskLevel: 'high', status: 'open', amount: 100, date: '2026-03-01', description: '' },
  { id: 'CASE-002', customerName: 'Bob', riskLevel: 'low', status: 'closed', amount: 200, date: '2026-03-02', description: '' },
]

describe('CaseList', () => {
  it('renders all cases initially', () => {
    render(<CaseList initialCases={mockCases} />)
    expect(screen.getByText(/Alice/i)).toBeInTheDocument()
    expect(screen.getByText(/Bob/i)).toBeInTheDocument()
  })

  it('filters cases by customer name', () => {
    render(<CaseList initialCases={mockCases} />)
    const searchInput = screen.getByPlaceholderText(/Search cases/i)
    
    fireEvent.change(searchInput, { target: { value: 'Alice' } })
    expect(screen.getByText(/Alice/i)).toBeInTheDocument()
    expect(screen.queryByText(/Bob/i)).not.toBeInTheDocument()
  })

  it('filters cases by ID', () => {
    render(<CaseList initialCases={mockCases} />)
    const searchInput = screen.getByPlaceholderText(/Search cases/i)
    
    fireEvent.change(searchInput, { target: { value: 'CASE-002' } })
    expect(screen.getByText(/Bob/i)).toBeInTheDocument()
    expect(screen.queryByText(/Alice/i)).not.toBeInTheDocument()
  })

  it('shows "No cases found" when search has no matches', () => {
    render(<CaseList initialCases={mockCases} />)
    const searchInput = screen.getByPlaceholderText(/Search cases/i)
    
    fireEvent.change(searchInput, { target: { value: 'Nonexistent' } })
    expect(screen.getByText(/No cases found/i)).toBeInTheDocument()
    expect(screen.queryByText(/Alice/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/Bob/i)).not.toBeInTheDocument()
  })
})
