import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// POST /api/cases/[id]/assign
export async function POST(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params
  const caseIndex = db.cases.findIndex((c) => c.caseId === id)
  if (caseIndex === -1) {
    return NextResponse.json({ error: 'Case not found' }, { status: 404 })
  }

  const body = await req.json().catch(() => ({}))
  const { assignTo } = body

  if (!assignTo) {
    return NextResponse.json({ error: 'assignTo is required' }, { status: 400 })
  }

  db.cases[caseIndex].assignedTo = assignTo
  db.cases[caseIndex].auditTrail.push({
    id: `AUD-${Date.now()}`,
    event: `Case assigned to ${assignTo}`,
    actor: 'analyst',
    timestamp: new Date().toISOString(),
    correlationId: `CORR-${id}-ASSIGN`,
  })

  return NextResponse.json({ success: true, assignedTo: assignTo })
}
