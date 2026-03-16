import { Analytics } from '@/types/alert'

async function getAnalytics(): Promise<Analytics> {
  const res = await fetch(`${process.env.NEXT_PUBLIC_BASE_URL ?? 'http://localhost:3000'}/api/analytics`, {
    cache: 'no-store',
  })
  return res.json()
}

export default async function AnalyticsPage() {
  const data = await getAnalytics()

  return (
    <div className="max-w-[1400px] mx-auto flex flex-col gap-4">
      {/* Page Header */}
      <div className="mb-2">
        <h1 className="text-xl font-bold text-slate-900">Analytics</h1>
        <p className="text-sm text-slate-500 mt-1">Aggregated fraud trends and operational insights</p>
      </div>

      {/* Top Stats Row */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
          <div className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">
            Avg. Time to Close
          </div>
          <div className="text-4xl font-bold text-slate-900">
            {data.avgTimeToCloseHours}
            <span className="text-xl font-normal text-slate-400 ml-1">hrs</span>
          </div>
        </div>
        <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
          <div className="text-xs font-bold tracking-widest text-slate-400 uppercase mb-4">
            Escalation Rate
          </div>
          <div className="text-4xl font-bold text-slate-900">
            {data.escalationRatePct}
            <span className="text-xl font-normal text-slate-400 ml-1">%</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-12 gap-4">
        {/* Alerts by Severity */}
        <div className="col-span-12 lg:col-span-5 bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
          <h2 className="text-sm font-bold tracking-widest text-slate-800 uppercase mb-6">Alerts by Severity</h2>
          <div className="flex flex-col gap-4">
            {data.alertsBySeverity.map((item) => {
              const colorMap: Record<string, string> = {
                CRITICAL: 'bg-red-500',
                HIGH: 'bg-orange-400',
                MEDIUM: 'bg-amber-400',
                LOW: 'bg-emerald-500',
              }
              const total = data.alertsBySeverity.reduce((s, i) => s + i.count, 0)
              const pct = Math.round((item.count / total) * 100)
              return (
                <div key={item.severity}>
                  <div className="flex justify-between items-center mb-1.5">
                    <span className="text-xs font-bold text-slate-600">{item.severity}</span>
                    <span className="text-xs font-bold text-slate-400">{item.count}</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2">
                    <div
                      className={`h-2 rounded-full ${colorMap[item.severity] ?? 'bg-slate-400'}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* Top Flags */}
        <div className="col-span-12 lg:col-span-7 bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
          <h2 className="text-sm font-bold tracking-widest text-slate-800 uppercase mb-6">Top Fraud Flags</h2>
          <div className="flex flex-col gap-4">
            {data.topFlags.map((flag, i) => (
              <div key={flag.flag} className="flex items-center gap-4">
                <span className="text-xs font-bold text-slate-400 w-6 text-right">{i + 1}</span>
                <div className="flex-1">
                  <div className="flex justify-between mb-1.5">
                    <span className="text-xs font-bold text-slate-600 font-mono">{flag.flag}</span>
                    <span className="text-xs text-slate-400 font-bold">{flag.count}</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2">
                    <div
                      className="h-2 rounded-full bg-blue-400"
                      style={{ width: `${(flag.count / data.topFlags[0].count) * 100}%` }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 30-Day Trend Table */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-sm font-bold tracking-widest text-slate-800 uppercase mb-6">Daily Alert Volume (Last 30 Days)</h2>
        <div className="flex items-end gap-1.5 h-36">
          {data.trendsLast30Days.map((day) => {
            const max = Math.max(...data.trendsLast30Days.map(d => d.count))
            const pct = (day.count / max) * 100
            return (
              <div
                key={day.date}
                className="flex-1 bg-blue-400 rounded-t-sm hover:bg-blue-500 transition-colors"
                style={{ height: `${pct}%` }}
                title={`${day.date}: ${day.count} alerts`}
              />
            )
          })}
        </div>
        <div className="flex justify-between text-[10px] font-bold text-slate-400 tracking-wider mt-3">
          <span>{data.trendsLast30Days[0]?.date}</span>
          <span>{data.trendsLast30Days[14]?.date}</span>
          <span>{data.trendsLast30Days[29]?.date}</span>
        </div>
      </div>

      {/* Footer */}
      <div className="flex justify-between items-center text-[10px] font-bold tracking-widest text-[#94a3b8] py-4 border-t border-slate-200 mt-2 uppercase">
        <div className="flex gap-4">
          <span>&copy; 2026 Sentinel Case Management System</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
          <span>ALL SYSTEMS OPERATIONAL</span>
        </div>
      </div>
    </div>
  )
}
