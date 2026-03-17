import { NextRequest, NextResponse } from 'next/server'
import { isApiConnected, proxyGet } from '@/lib/apiProxy'
import { db } from '@/lib/mockDb'

// GET /api/cases/[id]
export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params

  if (isApiConnected()) {
    try {
      const res = await proxyGet(`/api/cases/${id}`)
      const data = await res.json()
      return NextResponse.json(data, { status: res.status })
    } catch (err) {
      console.error('[cases/id] Java API unreachable, falling back to mock:', err)
    }
  }

  const found = db.cases.find((c) => c.caseId === id)
  if (!found) {
    return NextResponse.json({ error: 'Case not found' }, { status: 404 })
  }
  return NextResponse.json(found)
}
