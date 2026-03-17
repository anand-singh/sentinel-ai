import { NextRequest, NextResponse } from 'next/server'
import { isApiConnected, proxyPost } from '@/lib/apiProxy'
import { db } from '@/lib/mockDb'

// POST /api/cases/[id]/close
export async function POST(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params

  if (isApiConnected()) {
    try {
      const res = await proxyPost(`/api/cases/${id}/close`)
      const data = await res.json()
      return NextResponse.json(data, { status: res.status })
    } catch (err) {
      console.error('[cases/close] Java API unreachable, falling back to mock:', err)
    }
  }

  const caseIndex = db.cases.findIndex((c) => c.caseId === id)
  if (caseIndex === -1) {
    return NextResponse.json({ error: 'Case not found' }, { status: 404 })
  }

  db.cases[caseIndex].status = 'CLOSED'
  db.cases[caseIndex].auditTrail.push({
    id: `AUD-${Date.now()}`,
    event: 'Case closed by analyst',
    actor: 'analyst',
    timestamp: new Date().toISOString(),
    correlationId: `CORR-${id}-CLOSE`,
  })

  const alertIndex = db.alerts.findIndex((a) => a.id === db.cases[caseIndex].alert.id)
  if (alertIndex !== -1) {
    db.alerts[alertIndex].status = 'CLOSED'
  }

  return NextResponse.json({ success: true, status: 'CLOSED' })
}
