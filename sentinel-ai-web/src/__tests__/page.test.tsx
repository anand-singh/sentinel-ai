import { render, screen } from '@testing-library/react'
import Home from '@/app/page'
import * as casesService from '@/services/cases'

jest.mock('@/services/cases')

describe('Home', () => {
  it('renders the fraud cases heading', async () => {
    (casesService.getCases as jest.Mock).mockResolvedValue([
      { id: '1', customerName: 'Alice', riskLevel: 'low', status: 'open', amount: 0, date: '2026-01-01', description: '' }
    ])

    // Workaround for testing async Server Components in Jest/RTL
    const Page = await Home()
    render(Page)
    
    expect(screen.getByText(/Fraud Cases/i)).toBeInTheDocument()
  })
})
