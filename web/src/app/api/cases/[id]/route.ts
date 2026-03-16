import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// GET /api/cases/[id]
// Returns full case detail: agent outputs, evidence, actions, audit trail, related transactions
export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params
  const found = db.cases.find((c) => c.caseId === id)
  if (!found) {
    return NextResponse.json({ error: 'Case not found' }, { status: 404 })
  }
  return NextResponse.json(found)
}
