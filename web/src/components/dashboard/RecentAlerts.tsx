import { ChevronRight } from 'lucide-react'

export function RecentAlerts() {
  const alerts = [
    {
      status: 'REVIEWING',
      statusColor: 'bg-orange-100 text-orange-700',
      severity: 'Critical',
      severityColor: 'bg-red-500',
      caseId: '#F-9204',
      customerAvatar: 'bg-emerald-700',
      customerInitials: 'SJ',
      customerName: 'Sarah Jenkins',
      time: '2m ago'
    },
    {
      status: 'QUEUED',
      statusColor: 'bg-blue-50 text-blue-600',
      severity: 'Medium',
      severityColor: 'bg-amber-400',
      caseId: '#F-9198',
      customerAvatar: 'bg-slate-200 text-slate-600',
      customerInitials: 'RC',
      customerName: 'Robert Chen',
      time: '15m ago'
    },
    {
      status: 'RESOLVED',
      statusColor: 'bg-slate-100 text-slate-600',
      severity: 'Low',
      severityColor: 'bg-emerald-500',
      caseId: '#F-9195',
      customerAvatar: 'bg-slate-800 text-white',
      customerInitials: 'EW',
      customerName: 'Emily Watson',
      time: '42m ago'
    },
    {
      status: 'REVIEWING',
      statusColor: 'bg-orange-100 text-orange-700',
      severity: 'Critical',
      severityColor: 'bg-red-500',
      caseId: '#F-9192',
      customerAvatar: 'bg-black text-white',
      customerInitials: 'MT',
      customerName: 'Michael Thorne',
      time: '1h ago'
    }
  ]

  return (
    <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-sm font-bold tracking-widest text-[#1e293b]">RECENT ALERTS QUEUE</h3>
        <button className="text-sm font-bold text-blue-600 hover:text-blue-700">View All</button>
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
            {alerts.map((alert, i) => (
              <tr key={i} className="group hover:bg-slate-50 transition-colors cursor-pointer">
                <td className="py-4">
                  <span className={`text-[10px] font-bold px-2.5 py-1 rounded tracking-wider ${alert.statusColor}`}>
                    {alert.status}
                  </span>
                </td>
                <td className="py-4">
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${alert.severityColor}`}></span>
                    <span className="text-slate-700 font-medium">{alert.severity}</span>
                  </div>
                </td>
                <td className="py-4">
                  <span className="font-medium text-slate-700 font-mono text-xs">{alert.caseId}</span>
                </td>
                <td className="py-4">
                  <div className="flex items-center gap-3">
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold ${alert.customerAvatar}`}>
                       {alert.customerInitials}
                    </div>
                    <span className="font-medium text-slate-700">{alert.customerName}</span>
                  </div>
                </td>
                <td className="py-4">
                  <span className="text-slate-500 font-medium text-xs">{alert.time}</span>
                </td>
                <td className="py-4 text-right pr-2">
                   <ChevronRight size={16} className="inline-block text-slate-300 group-hover:text-slate-500 transition-colors" />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
