import { getCases } from '../cases'

describe('cases service', () => {
  it('returns a list of cases', async () => {
    const cases = await getCases()
    expect(cases.length).toBeGreaterThan(0)
    expect(cases[0]).toHaveProperty('id')
    expect(cases[0]).toHaveProperty('status')
    expect(cases[0]).toHaveProperty('riskLevel')
  })
})
