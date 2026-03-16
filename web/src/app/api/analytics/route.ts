import { NextResponse } from 'next/server'
import { db } from '@/lib/mockDb'

// GET /api/analytics
// Returns: alerts by severity, trends over 30 days, top flags, escalation rate
export async function GET() {
  return NextResponse.json(db.analytics)
}
