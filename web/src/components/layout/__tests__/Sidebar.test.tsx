import { render, screen } from '@testing-library/react'
import { Sidebar } from '../Sidebar'

describe('Sidebar', () => {
  it('renders navigation links', () => {
    render(<Sidebar />)
    expect(screen.getByText(/Dashboard/i)).toBeInTheDocument()
    expect(screen.getByText(/Cases/i)).toBeInTheDocument()
    expect(screen.getByText(/Analytics/i)).toBeInTheDocument()
  })
})
