'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { ChevronRight } from 'lucide-react'
import { Alert } from '@/types/alert'

function severityDot(s: string) {
  switch (s) {
    case 'CRITICAL': return 'bg-red-500'
    case 'HIGH':     return 'bg-amber-500'
    case 'MEDIUM':   return 'bg-amber-400'
    default:         return 'bg-emerald-500'
  }
}

function statusStyle(s: string) {
  switch (s) {
    case 'REVIEWING': return 'bg-orange-100 text-orange-700'
    case 'QUEUED':    return 'bg-blue-50 text-blue-600'
    case 'RESOLVED':  return 'bg-slate-100 text-slate-600'
    default:          return 'bg-slate-100 text-slate-600'
  }
}

const AVATAR_COLORS = [
  'bg-emerald-700', 'bg-blue-700', 'bg-purple-700',
  'bg-slate-700', 'bg-rose-700', 'bg-teal-700',
]

function avatarColor(id: string) {
  return AVATAR_COLORS[id.charCodeAt(id.length - 1) % AVATAR_COLORS.length]
}

function initials(name: string) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
}

function timeAgo(ts: string): string {
  const diff = Date.now() - new Date(ts).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  return `${Math.floor(h / 24)}d ago`
}

export function RecentAlerts() {
  const router = useRouter()
  const [alerts, setAlerts] = useState<Alert[]>([])

  useEffect(() => {
    fetch('/api/alerts?limit=4&page=1')
      .then(r => r.json())
      .then(d => setAlerts(d.data ?? []))
      .catch(() => {/* silent – keep empty */})
  }, [])

  return (
    <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-sm font-bold tracking-widest text-[#1e293b]">RECENT ALERTS QUEUE</h3>
        <Link href="/analytics" className="text-sm font-bold text-blue-600 hover:text-blue-700">
          View All
        </Link>
      </div>

      <div className="w-full overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-[10px] font-bold tracking-widest text-[#94a3b8] uppercase border-b border-slate-100">
              <th className="pb-4 font-bold">STATUS</th>
              <th className="pb-4 font-bold">SEVERITY</th>
              <th className="pb-4 font-bold">CASE ID</th>
              <th className="pb-4 font-bold">CUSTOMER</th>
              <th className="pb-4 font-bold">TIME</th>
              <th className="pb-4"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {alerts.map((alert) => {
              const href = alert.caseId ? `/cases/${alert.caseId}` : null
              return (
                <tr
                  key={alert.id}
                  className={`group hover:bg-slate-50 transition-colors ${href ? 'cursor-pointer' : ''}`}
                  onClick={() => href && router.push(href)}
                >
                  <td className="py-4">
                    <span className={`text-[10px] font-bold px-2.5 py-1 rounded tracking-wider ${statusStyle(alert.status)}`}>
                      {alert.status}
                    </span>
                  </td>
                  <td className="py-4">
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${severityDot(alert.severity)}`}></span>
                      <span className="text-slate-700 font-medium capitalize">
                        {alert.severity.charAt(0) + alert.severity.slice(1).toLowerCase()}
                      </span>
                    </div>
                  </td>
                  <td className="py-4">
                    <span className="font-medium text-slate-700 font-mono text-xs">
                      {alert.caseId ? `#${alert.caseId}` : alert.transactionId}
                    </span>
                  </td>
                  <td className="py-4">
                    <div className="flex items-center gap-3">
                      <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold text-white ${avatarColor(alert.customerId)}`}>
                        {initials(alert.customerName)}
                      </div>
                      <span className="font-medium text-slate-700">{alert.customerName}</span>
                    </div>
                  </td>
                  <td className="py-4">
                    <span className="text-slate-500 font-medium text-xs">{timeAgo(alert.timestamp)}</span>
                  </td>
                  <td className="py-4 text-right pr-2">
                    <ChevronRight size={16} className="inline-block text-slate-300 group-hover:text-slate-500 transition-colors" />
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
