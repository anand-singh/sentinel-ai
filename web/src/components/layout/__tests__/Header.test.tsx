import { render, screen } from '@testing-library/react'
import { Header } from '../Header'

describe('Header', () => {
  it('renders the application title', () => {
    render(<Header />)
    expect(screen.getByText(/Sentinel AI/i)).toBeInTheDocument()
  })
})
