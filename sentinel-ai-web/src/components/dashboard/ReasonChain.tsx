'use client'

import { useState } from 'react'
import { AlertTriangle, TrendingUp, Zap, CheckCircle, XCircle } from 'lucide-react'

interface Props {
  caseId?: string
}

type ActionState = 'idle' | 'loading' | 'approved' | 'rejected' | 'error'

export function ReasonChain({ caseId = 'F-9204' }: Props) {
  const [state, setState] = useState<ActionState>('idle')

  async function handleAction(action: 'close' | 'escalate') {
    setState('loading')
    try {
      const res = await fetch(`/api/cases/${caseId}/${action}`, { method: 'POST' })
      if (!res.ok) throw new Error('Request failed')
      setState(action === 'close' ? 'approved' : 'rejected')
    } catch {
      setState('error')
    }
  }

  return (
    <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm flex flex-col h-full">
      <h3 className="text-sm font-bold tracking-widest text-[#1e293b] mb-6">REASON CHAIN</h3>

      <div className="flex-1 flex flex-col gap-6 relative before:absolute before:inset-y-0 before:left-3.5 before:w-px before:bg-slate-200">

        <div className="relative flex gap-4">
          <div className="w-7 h-7 bg-red-500 rounded-full flex items-center justify-center shrink-0 z-10 shadow-sm shadow-red-200">
            <AlertTriangle size={14} className="text-white" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900 leading-tight mb-1">Unusual Login Location</h4>
            <div className="text-xs text-slate-500 mb-1">
              IP: 192.168.1.1 (Lagos, NG) vs History: (New York, US)
            </div>
            <div className="text-[10px] text-slate-400 font-medium">Triggered 2m ago</div>
          </div>
        </div>

        <div className="relative flex gap-4">
          <div className="w-7 h-7 bg-amber-500 rounded-full flex items-center justify-center shrink-0 z-10 shadow-sm shadow-amber-200">
            <TrendingUp size={14} className="text-white" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900 leading-tight mb-1">High-Value Transaction</h4>
            <div className="text-xs text-slate-500 mb-1">
               $4,950.00 is 340% above user avg.
            </div>
            <div className="text-[10px] text-slate-400 font-medium">Triggered 2m ago</div>
          </div>
        </div>

        <div className="relative flex gap-4">
          <div className="w-7 h-7 bg-emerald-500 rounded-full flex items-center justify-center shrink-0 z-10 shadow-sm shadow-emerald-200">
            <Zap size={14} className="text-white" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900 leading-tight mb-1">Velocity Check Failed</h4>
            <div className="text-xs text-slate-500 mb-1">
              4 transactions in &lt; 30 seconds.
            </div>
            <div className="text-[10px] text-slate-400 font-medium">Triggered 3m ago</div>
          </div>
        </div>

      </div>

      {state === 'approved' && (
        <div className="mt-6 flex items-center gap-2 text-emerald-700 bg-emerald-50 rounded p-3 text-sm font-medium">
          <CheckCircle size={16} />
          Case {caseId} closed successfully.
        </div>
      )}

      {state === 'rejected' && (
        <div className="mt-6 flex items-center gap-2 text-orange-700 bg-orange-50 rounded p-3 text-sm font-medium">
          <XCircle size={16} />
          Case {caseId} escalated to senior review.
        </div>
      )}

      {state === 'error' && (
        <div className="mt-6 text-red-600 bg-red-50 rounded p-3 text-sm font-medium">
          Action failed — please try again.
        </div>
      )}

      {(state === 'idle' || state === 'loading' || state === 'error') && (
        <div className="mt-8 flex gap-3">
          <button
            disabled={state === 'loading'}
            onClick={() => handleAction('escalate')}
            className="flex-1 bg-red-600 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold text-xs tracking-wider py-3 rounded text-center transition-colors"
          >
            REJECT CASE
          </button>
          <button
            disabled={state === 'loading'}
            onClick={() => handleAction('close')}
            className="flex-1 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold text-xs tracking-wider py-3 rounded text-center transition-colors"
          >
            APPROVE
          </button>
        </div>
      )}
    </div>
  )
}
