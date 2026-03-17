import { render, screen } from '@testing-library/react'
import { DashboardLayout } from '../DashboardLayout'

// Mock Sidebar and Header to isolate Layout test
jest.mock('../Sidebar', () => ({
  Sidebar: () => <div data-testid="sidebar" />
}))
jest.mock('../Header', () => ({
  Header: () => <div data-testid="header" />
}))

describe('DashboardLayout', () => {
  it('renders children and layout components', () => {
    render(
      <DashboardLayout>
        <div data-testid="child">Content</div>
      </DashboardLayout>
    )
    expect(screen.getByTestId('sidebar')).toBeInTheDocument()
    expect(screen.getByTestId('header')).toBeInTheDocument()
    expect(screen.getByTestId('child')).toBeInTheDocument()
  })
})
