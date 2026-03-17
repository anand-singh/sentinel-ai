import { NextRequest, NextResponse } from 'next/server'
import { isApiConnected, proxyPostRaw } from '@/lib/apiProxy'

// POST /api/ingest
// Forwards a raw transaction JSON to the Java API orchestrator pipeline.
// Returns the new CaseDetail on success.
export async function POST(req: NextRequest) {
  const rawBody = await req.text()

  if (!isApiConnected()) {
    return NextResponse.json(
      { error: 'Java API not configured. Set SENTINEL_API_URL environment variable.' },
      { status: 503 }
    )
  }

  try {
    const res = await proxyPostRaw('/api/ingest', rawBody)
    const data = await res.json()
    return NextResponse.json(data, { status: res.status })
  } catch (err) {
    console.error('[ingest] Java API error:', err)
    return NextResponse.json(
      { error: 'Failed to reach orchestrator. Is the Java API running?' },
      { status: 502 }
    )
  }
}
