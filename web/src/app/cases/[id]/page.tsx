import { notFound } from 'next/navigation'
import Link from 'next/link'
import { ChevronLeft, User, GitBranch, Clock } from 'lucide-react'
import { AgentTabs } from '@/components/dashboard/AgentTabs'
import { AuditTimeline } from '@/components/dashboard/AuditTimeline'
import { QuickActions } from '@/components/dashboard/QuickActions'
import { CaseDetail } from '@/types/alert'

async function getCaseDetail(id: string): Promise<CaseDetail | null> {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_BASE_URL ?? 'http://localhost:3000'}/api/cases/${id}`,
    { cache: 'no-store' }
  )
  if (!res.ok) return null
  return res.json()
}

const severityColors: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-700',
  HIGH: 'bg-orange-100 text-orange-700',
  MEDIUM: 'bg-amber-100 text-amber-700',
  LOW: 'bg-emerald-100 text-emerald-700',
}

const statusColors: Record<string, string> = {
  OPEN: 'bg-blue-50 text-blue-700',
  IN_REVIEW: 'bg-orange-50 text-orange-700',
  ESCALATED: 'bg-red-50 text-red-700',
  CLOSED: 'bg-slate-100 text-slate-600',
}

export default async function CaseDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params
  const c = await getCaseDetail(id)

  if (!c) notFound()

  return (
    <div className="max-w-[1400px] mx-auto flex flex-col gap-4">
      {/* Back nav */}
      <div className="flex items-center gap-3">
        <Link href="/" className="flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-slate-700 transition-colors">
          <ChevronLeft size={14} /> Back to Dashboard
        </Link>
        <span className="text-slate-300">/</span>
        <span className="text-xs font-bold text-slate-800">Case #{c.caseId}</span>
      </div>

      {/* Header row */}
      <div className="flex flex-wrap gap-3 items-center">
        <h1 className="text-xl font-bold text-slate-900">Case #{c.caseId}</h1>
        <span className={`text-[10px] font-bold tracking-widest px-2.5 py-1 rounded uppercase ${statusColors[c.status] ?? 'bg-slate-100 text-slate-600'}`}>
          {c.status.replace('_', ' ')}
        </span>
        <span className={`text-[10px] font-bold tracking-widest px-2.5 py-1 rounded uppercase ${severityColors[c.alert.severity] ?? ''}`}>
          {c.alert.severity}
        </span>
      </div>

      <div className="grid grid-cols-12 gap-4">
        {/* LEFT: Main detail */}
        <div className="col-span-12 xl:col-span-8 flex flex-col gap-4">

          {/* Alert Summary */}
          <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
            <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">Alert Summary</h3>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div>
                <div className="text-[10px] font-bold tracking-widest text-slate-400 uppercase mb-1">Transaction</div>
                <div className="font-mono font-semibold text-slate-900 text-sm">{c.alert.transactionId}</div>
              </div>
              <div>
                <div className="text-[10px] font-bold tracking-widest text-slate-400 uppercase mb-1">Customer</div>
                <div className="font-semibold text-slate-900 text-sm">{c.alert.customerName}</div>
              </div>
              <div>
                <div className="text-[10px] font-bold tracking-widest text-slate-400 uppercase mb-1">Risk Score</div>
                <div className="font-bold text-red-600 text-xl">{c.alert.finalRiskScore}</div>
              </div>
              <div>
                <div className="text-[10px] font-bold tracking-widest text-slate-400 uppercase mb-1">Recommended Action</div>
                <div className="text-xs font-bold font-mono text-slate-700 mt-1 bg-slate-50 border border-slate-200 px-2 py-1 rounded">{c.alert.recommendedAction}</div>
              </div>
            </div>
          </div>

          {/* Agent Tabs */}
          {c.agentOutputs.length > 0 && (
            <div>
              <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-3">Agent Reasoning</h3>
              <AgentTabs outputs={c.agentOutputs} />
            </div>
          )}

          {/* Related Transactions */}
          {c.relatedTransactionIds.length > 0 && (
            <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
              <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">Related Transactions</h3>
              <div className="flex flex-wrap gap-2">
                {c.relatedTransactionIds.map((txId) => (
                  <span key={txId} className="text-xs font-mono font-semibold text-blue-700 bg-blue-50 border border-blue-200 px-3 py-1.5 rounded-md">
                    {txId}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Audit Trail */}
          <AuditTimeline entries={c.auditTrail} />

          {/* Analyst Notes */}
          {c.notes.length > 0 && (
            <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
              <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">Analyst Notes</h3>
              <div className="flex flex-col gap-3">
                {c.notes.map((note, i) => (
                  <div key={i} className="text-sm text-slate-700 bg-amber-50 border border-amber-200 rounded-md px-4 py-3">
                    {note}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* RIGHT: Sidebar metadata */}
        <div className="col-span-12 xl:col-span-4 flex flex-col gap-4">
          <QuickActions caseId={c.caseId} currentStatus={c.status} />

          {/* Case Metadata */}
          <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
            <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-5">Case Metadata</h3>
            <div className="flex flex-col gap-4">
              <div className="flex items-start gap-3">
                <User size={14} className="text-slate-400 mt-0.5 shrink-0" />
                <div>
                  <div className="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Assigned To</div>
                  <div className="text-sm font-semibold text-slate-800 mt-0.5">{c.assignedTo ?? 'Unassigned'}</div>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <Clock size={14} className="text-slate-400 mt-0.5 shrink-0" />
                <div>
                  <div className="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Opened</div>
                  <div className="text-sm font-semibold text-slate-800 mt-0.5">{new Date(c.alert.timestamp).toLocaleString()}</div>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <GitBranch size={14} className="text-slate-400 mt-0.5 shrink-0" />
                <div>
                  <div className="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Policy Version</div>
                  <div className="text-sm font-mono font-semibold text-emerald-700 mt-0.5">{c.policyVersion}</div>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <GitBranch size={14} className="text-slate-400 mt-0.5 shrink-0" />
                <div>
                  <div className="text-[10px] font-bold tracking-wider text-slate-400 uppercase">Agent Version</div>
                  <div className="text-sm font-mono font-semibold text-blue-700 mt-0.5">{c.agentVersion}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Actions Executed */}
          {c.actionsExecuted.length > 0 && (
            <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
              <h3 className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">Actions Taken by Agent</h3>
              <div className="flex flex-col gap-3">
                {c.actionsExecuted.map((action) => (
                  <div key={action.id} className="flex items-start gap-3">
                    <div className="w-2 h-2 rounded-full bg-blue-500 mt-1.5 shrink-0" />
                    <div>
                      <div className="text-xs font-bold font-mono text-slate-700">{action.type}</div>
                      <div className="text-[10px] text-slate-400 mt-0.5">
                        {new Date(action.timestamp).toLocaleTimeString()} · {action.performedBy}
                      </div>
                      {action.note && <div className="text-xs text-slate-500 italic mt-1">{action.note}</div>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
