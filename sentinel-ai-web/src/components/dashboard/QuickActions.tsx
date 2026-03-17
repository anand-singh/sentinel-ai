'use client'

import { useState } from 'react'
import { UserCheck, MessageSquarePlus, AlertTriangle, XCircle } from 'lucide-react'

interface QuickActionsProps {
  caseId: string
  currentStatus: string
  onUpdate?: () => void
}

export function QuickActions({ caseId, currentStatus }: QuickActionsProps) {
  const [loading, setLoading] = useState<string | null>(null)
  const [toast, setToast] = useState<{ msg: string; type: 'success' | 'error' } | null>(null)
  const [noteText, setNoteText] = useState('')
  const [assignText, setAssignText] = useState('')
  const [showAssign, setShowAssign] = useState(false)
  const [showNote, setShowNote] = useState(false)

  const showToast = (msg: string, type: 'success' | 'error') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const post = async (endpoint: string, body?: object) => {
    setLoading(endpoint)
    try {
      const res = await fetch(`/api/cases/${caseId}/${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined,
      })
      const json = await res.json()
      if (!res.ok) throw new Error(json.error ?? 'Request failed')
      showToast(`✓ ${endpoint} successful`, 'success')
    } catch (e) {
      showToast((e as Error).message, 'error')
    } finally {
      setLoading(null)
    }
  }

  return (
    <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-6 relative">
      <h3 className="text-sm font-bold tracking-widest text-[#1e293b] uppercase mb-5">Quick Actions</h3>

      {/* Toast */}
      {toast && (
        <div className={`absolute top-3 right-3 text-xs font-bold px-3 py-2 rounded shadow-lg z-10 ${
          toast.type === 'success' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          {toast.msg}
        </div>
      )}

      <div className="flex flex-col gap-3">
        {/* Assign */}
        <div>
          <button
            onClick={() => { setShowAssign(!showAssign); setShowNote(false) }}
            className="w-full flex items-center gap-3 text-sm font-semibold text-slate-700 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-md px-4 py-2.5 transition-colors"
          >
            <UserCheck size={16} className="text-blue-500" />
            Assign to Analyst
          </button>
          {showAssign && (
            <div className="mt-2 flex gap-2">
              <input
                type="text"
                placeholder="Analyst name..."
                value={assignText}
                onChange={(e) => setAssignText(e.target.value)}
                className="flex-1 text-sm border border-slate-200 rounded-md px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-300"
              />
              <button
                disabled={!assignText || loading === 'assign'}
                onClick={() => { post('assign', { assignTo: assignText }); setAssignText(''); setShowAssign(false) }}
                className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-xs font-bold px-4 py-2 rounded-md transition-colors"
              >
                {loading === 'assign' ? '...' : 'Assign'}
              </button>
            </div>
          )}
        </div>

        {/* Add Note */}
        <div>
          <button
            onClick={() => { setShowNote(!showNote); setShowAssign(false) }}
            className="w-full flex items-center gap-3 text-sm font-semibold text-slate-700 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-md px-4 py-2.5 transition-colors"
          >
            <MessageSquarePlus size={16} className="text-indigo-500" />
            Add Note
          </button>
          {showNote && (
            <div className="mt-2 flex flex-col gap-2">
              <textarea
                placeholder="Analyst note..."
                rows={3}
                value={noteText}
                onChange={(e) => setNoteText(e.target.value)}
                className="text-sm border border-slate-200 rounded-md px-3 py-2 focus:outline-none focus:ring-1 focus:ring-indigo-300 resize-none"
              />
              <button
                disabled={!noteText || loading === 'note'}
                onClick={() => { post('note', { note: noteText }); setNoteText(''); setShowNote(false) }}
                className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white text-xs font-bold px-4 py-2 rounded-md transition-colors"
              >
                {loading === 'note' ? '...' : 'Save Note'}
              </button>
            </div>
          )}
        </div>

        {/* Escalate */}
        <button
          disabled={currentStatus === 'ESCALATED' || !!loading}
          onClick={() => post('escalate')}
          className="w-full flex items-center gap-3 text-sm font-semibold text-slate-700 bg-slate-50 hover:bg-amber-50 border border-slate-200 hover:border-amber-300 rounded-md px-4 py-2.5 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <AlertTriangle size={16} className="text-amber-500" />
          {loading === 'escalate' ? 'Escalating...' : 'Escalate Case'}
        </button>

        {/* Close */}
        <button
          disabled={currentStatus === 'CLOSED' || !!loading}
          onClick={() => post('close')}
          className="w-full flex items-center gap-3 text-sm font-semibold text-white bg-red-600 hover:bg-red-700 rounded-md px-4 py-2.5 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <XCircle size={16} />
          {loading === 'close' ? 'Closing...' : 'Close Case'}
        </button>
      </div>
    </div>
  )
}
