import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// POST /api/cases/[id]/close
export async function POST(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params
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

  // Also update the related alert status
  const alertIndex = db.alerts.findIndex((a) => a.id === db.cases[caseIndex].alert.id)
  if (alertIndex !== -1) {
    db.alerts[alertIndex].status = 'CLOSED'
  }

  return NextResponse.json({ success: true, status: 'CLOSED' })
}
