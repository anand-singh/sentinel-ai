'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'

const SAMPLE_TRANSACTION = JSON.stringify(
  {
    transaction_id: 'TX-DEMO-001',
    customer_id: 'CUST-SJ01',
    customer_name: 'Sarah Jenkins',
    amount: 4950.00,
    currency: 'USD',
    merchant: 'ElectroHub Lagos',
    merchant_category: 'ELECTRONICS',
    channel: 'CARD_NOT_PRESENT',
    country: 'NG',
    city: 'Lagos',
    device_id: 'DEV-NEW-9921',
    ip_address: '197.211.62.10',
    timestamp: '2026-03-17T09:04:00Z',
    customer_profile: {
      home_country: 'US',
      home_city: 'New York',
      avg_transaction_amount: 145.00,
      known_devices: ['DEV-IPHONE-001', 'DEV-MACBOOK-002'],
      last_login_location: 'New York, US',
      last_login_time: '2026-03-17T09:00:00Z',
    },
  },
  null,
  2
)

export function IngestTransactionModal() {
  const router = useRouter()
  const [open, setOpen] = useState(false)
  const [json, setJson] = useState(SAMPLE_TRANSACTION)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit() {
    setError(null)
    // Validate JSON
    try { JSON.parse(json) }
    catch { setError('Invalid JSON — please fix syntax errors.'); return }

    setLoading(true)
    try {
      const res = await fetch('/api/ingest', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: json,
      })
      const data = await res.json()
      if (!res.ok) {
        setError(data.error ?? 'Orchestrator returned an error.')
        return
      }
      setOpen(false)
      // Navigate to the new case detail page
      router.push(`/cases/${data.caseId}`)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Network error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {/* Trigger button */}
      <button
        onClick={() => setOpen(true)}
        className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        Analyze Transaction
      </button>

      {/* Modal overlay */}
      {open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl flex flex-col max-h-[90vh]">
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
              <div>
                <h2 className="text-base font-semibold text-slate-900">Run Fraud Pipeline</h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  Submit a transaction JSON to run the full 5-agent Sentinel orchestrator.
                </p>
              </div>
              <button
                onClick={() => setOpen(false)}
                className="text-slate-400 hover:text-slate-600 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Body */}
            <div className="flex-1 overflow-auto px-6 py-4">
              <label className="block text-xs font-semibold text-slate-600 mb-2 uppercase tracking-wide">
                Transaction JSON
              </label>
              <textarea
                className="w-full h-72 font-mono text-xs bg-slate-50 border border-slate-200 rounded-lg p-3 resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={json}
                onChange={(e) => setJson(e.target.value)}
                spellCheck={false}
              />
              {error && (
                <p className="mt-2 text-xs text-red-600 bg-red-50 rounded-lg px-3 py-2">
                  {error}
                </p>
              )}
            </div>

            {/* Footer */}
            <div className="flex items-center justify-between px-6 py-4 border-t border-slate-200 bg-slate-50 rounded-b-2xl">
              <button
                onClick={() => setJson(SAMPLE_TRANSACTION)}
                className="text-xs text-slate-500 hover:text-slate-700 underline transition-colors"
              >
                Reset to sample
              </button>
              <div className="flex gap-3">
                <button
                  onClick={() => setOpen(false)}
                  className="px-4 py-2 rounded-lg text-sm text-slate-600 hover:bg-slate-100 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleSubmit}
                  disabled={loading}
                  className="px-5 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60 transition-colors flex items-center gap-2"
                >
                  {loading ? (
                    <>
                      <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                      </svg>
                      Running pipeline…
                    </>
                  ) : (
                    'Run Pipeline'
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
