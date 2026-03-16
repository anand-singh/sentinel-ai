import { NextRequest, NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// GET /api/alerts
// Supports: ?page=1&limit=10&severity=CRITICAL&status=REVIEWING&q=searchterm
export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const page = parseInt(searchParams.get('page') ?? '1', 10)
  const limit = parseInt(searchParams.get('limit') ?? '10', 10)
  const severity = searchParams.get('severity')
  const status = searchParams.get('status')
  const q = searchParams.get('q')?.toLowerCase()

  let results = [...db.alerts]

  if (severity) results = results.filter((a) => a.severity === severity.toUpperCase())
  if (status) results = results.filter((a) => a.status === status.toUpperCase())
  if (q) {
    results = results.filter(
      (a) =>
        a.transactionId.toLowerCase().includes(q) ||
        a.customerId.toLowerCase().includes(q) ||
        a.customerName.toLowerCase().includes(q)
    )
  }

  const total = results.length
  const start = (page - 1) * limit
  const paged = results.slice(start, start + limit)

  return NextResponse.json({
    data: paged,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  })
}
