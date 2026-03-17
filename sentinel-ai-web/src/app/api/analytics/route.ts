import { NextResponse } from 'next/server'
import { isApiConnected, proxyGet } from '@/lib/apiProxy'
import { db } from '@/lib/mockDb'

// GET /api/analytics
export async function GET() {
  if (isApiConnected()) {
    try {
      const res = await proxyGet('/api/analytics')
      const data = await res.json()
      return NextResponse.json(data, { status: res.status })
    } catch (err) {
      console.error('[analytics] Java API unreachable, falling back to mock:', err)
    }
  }

  return NextResponse.json(db.analytics)
}
