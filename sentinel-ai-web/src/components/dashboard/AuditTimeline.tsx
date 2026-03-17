import { AuditEntry } from '@/types/alert'
import { ShieldCheck } from 'lucide-react'

interface AuditTimelineProps {
  entries: AuditEntry[]
}

export function AuditTimeline({ entries }: AuditTimelineProps) {
  return (
    <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-6">
      <h3 className="text-sm font-bold tracking-widest text-[#1e293b] uppercase mb-6">Audit Trail</h3>
      <div className="relative">
        {/* Vertical line */}
        <div className="absolute left-3.5 top-0 bottom-0 w-px bg-slate-200" />

        <div className="flex flex-col gap-6">
          {entries.map((entry, i) => (
            <div key={entry.id} className="relative flex gap-4">
              {/* Timeline dot */}
              <div
                className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 z-10 shadow-sm ${
                  i === 0 ? 'bg-blue-500 shadow-blue-200' : 'bg-slate-100 border border-slate-300'
                }`}
              >
                <ShieldCheck size={12} className={i === 0 ? 'text-white' : 'text-slate-500'} />
              </div>

              {/* Entry details */}
              <div className="flex-1 pb-1">
                <div className="flex items-start justify-between gap-4">
                  <span className="text-sm font-semibold text-slate-900">{entry.event}</span>
                  <span className="text-[10px] text-slate-400 font-medium shrink-0">
                    {new Date(entry.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                <div className="flex items-center gap-3 mt-1.5 flex-wrap">
                  <span className="text-xs text-slate-500">
                    by <span className="font-medium text-slate-700">{entry.actor}</span>
                  </span>
                  <span className="text-[10px] font-mono text-slate-400 bg-slate-50 px-2 py-0.5 rounded border border-slate-200">
                    {entry.correlationId}
                  </span>
                  {entry.policyVersion && (
                    <span className="text-[10px] font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                      policy {entry.policyVersion}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
