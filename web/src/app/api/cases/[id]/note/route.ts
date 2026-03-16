import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// POST /api/cases/[id]/note
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
  const { note } = body

  if (!note || typeof note !== 'string') {
    return NextResponse.json({ error: 'note is required' }, { status: 400 })
  }

  db.cases[caseIndex].notes.push(note)
  db.cases[caseIndex].auditTrail.push({
    id: `AUD-${Date.now()}`,
    event: `Note added by analyst`,
    actor: 'analyst',
    timestamp: new Date().toISOString(),
    correlationId: `CORR-${id}-NOTE`,
  })

  return NextResponse.json({ success: true, note })
}
