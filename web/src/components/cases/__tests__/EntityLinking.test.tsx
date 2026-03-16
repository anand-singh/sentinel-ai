import { render, screen } from '@testing-library/react'
import { EntityLinking } from '../EntityLinking'
import { LinkedEntity } from '@/types/case'

const mockEntities: LinkedEntity[] = [
  { id: 'ACC-501', type: 'account', relationship: 'Same Device', riskLevel: 'high' },
  { id: 'ACC-502', type: 'account', relationship: 'Same IP', riskLevel: 'medium' },
]

describe('EntityLinking', () => {
  it('renders a list of linked entities', () => {
    render(<EntityLinking entities={mockEntities} />)
    expect(screen.getByText(/ACC-501/i)).toBeInTheDocument()
    expect(screen.getByText(/Same Device/i)).toBeInTheDocument()
    expect(screen.getByText(/Same IP/i)).toBeInTheDocument()
  })
})
