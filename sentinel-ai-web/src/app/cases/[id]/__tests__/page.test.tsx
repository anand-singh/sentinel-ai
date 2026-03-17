import { render, screen } from '@testing-library/react'
import CaseDetailsPage from '../page'
import * as casesService from '@/services/cases'

jest.mock('@/services/cases')

describe('CaseDetailsPage', () => {
  it('renders case details for a given ID', async () => {
    const mockCase = {
      id: 'CASE-001',
      customerName: 'John Doe',
      riskLevel: 'high',
      status: 'open',
      amount: 1000,
      date: '2026-03-15',
      description: 'Test description'
    };
    (casesService.getCase as jest.Mock).mockResolvedValue(mockCase)

    // In Next.js 15, params is often a Promise, but in unit tests we can pass it directly or as a Promise
    const Page = await CaseDetailsPage({ params: Promise.resolve({ id: 'CASE-001' }) })
    render(Page)

    expect(screen.getByText(/CASE-001/i)).toBeInTheDocument()
    expect(screen.getByText(/John Doe/i)).toBeInTheDocument()
    expect(screen.getByText(/Test description/i)).toBeInTheDocument()
    expect(screen.getByText(/Transaction History/i)).toBeInTheDocument()
    expect(screen.getByText(/Entity Linking/i)).toBeInTheDocument()
  })
})
