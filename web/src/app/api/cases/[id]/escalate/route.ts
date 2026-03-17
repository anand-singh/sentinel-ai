import { NextRequest, NextResponse } from 'next/server'
import { isApiConnected, proxyPost } from '@/lib/apiProxy'
import { db } from '@/lib/mockDb'

// POST /api/cases/[id]/escalate
export async function POST(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params

  if (isApiConnected()) {
    try {
      const res = await proxyPost(`/api/cases/${id}/escalate`)
      const data = await res.json()
      return NextResponse.json(data, { status: res.status })
    } catch (err) {
      console.error('[cases/escalate] Java API unreachable, falling back to mock:', err)
    }
  }

  const caseIndex = db.cases.findIndex((c) => c.caseId === id)
  if (caseIndex === -1) {
    return NextResponse.json({ error: 'Case not found' }, { status: 404 })
  }

  db.cases[caseIndex].status = 'ESCALATED'
  db.cases[caseIndex].auditTrail.push({
    id: `AUD-${Date.now()}`,
    event: 'Case escalated to high priority',
    actor: 'analyst',
    timestamp: new Date().toISOString(),
    correlationId: `CORR-${id}-ESCALATE`,
  })

  return NextResponse.json({ success: true, status: 'ESCALATED' })
}
