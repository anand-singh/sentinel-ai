import { Info, Clock, BarChart2, DollarSign } from 'lucide-react'

export function SummaryCards() {
  return (
    <div className="grid grid-cols-4 gap-4 mb-4">
      {/* Total Alerts */}
      <div className="bg-white rounded-lg p-5 border border-slate-200 shadow-sm flex flex-col justify-between h-32">
        <div className="flex justify-between items-center text-slate-500 text-xs font-semibold tracking-wider">
          <span>TOTAL ALERTS</span>
          <div className="w-5 h-5 rounded-full border border-blue-200 text-blue-500 flex items-center justify-center">
             <Info size={12} />
          </div>
        </div>
        <div className="flex items-baseline gap-2">
          <span className="text-3xl font-bold text-slate-900">1,284</span>
          <span className="text-sm font-medium text-red-500">+12%</span>
        </div>
      </div>

      {/* Pending Reviews */}
      <div className="bg-white rounded-lg p-5 border border-slate-200 shadow-sm flex flex-col justify-between h-32">
        <div className="flex justify-between items-center text-slate-500 text-xs font-semibold tracking-wider">
          <span>PENDING REVIEWS</span>
          <div className="w-5 h-5 rounded-full border border-orange-200 text-orange-500 flex items-center justify-center">
             <Clock size={12} />
          </div>
        </div>
        <div className="flex items-baseline gap-2">
          <span className="text-3xl font-bold text-slate-900">42</span>
          <span className="text-sm font-medium text-slate-400">No change</span>
        </div>
      </div>

      {/* Avg. Risk Score */}
      <div className="bg-white rounded-lg p-5 border border-slate-200 shadow-sm flex flex-col justify-between h-32">
        <div className="flex justify-between items-center text-slate-500 text-xs font-semibold tracking-wider">
          <span>AVG. RISK SCORE</span>
           <div className="w-5 h-5 rounded-full border border-slate-200 text-slate-500 flex items-center justify-center">
             <BarChart2 size={12} />
          </div>
        </div>
        <div className="flex items-baseline gap-2">
          <span className="text-3xl font-bold text-slate-900">68.5</span>
          <span className="text-sm font-medium text-emerald-500">-4.2%</span>
        </div>
      </div>

      {/* Prevented Loss */}
      <div className="bg-white rounded-lg p-5 border border-slate-200 shadow-sm flex flex-col justify-between h-32">
        <div className="flex justify-between items-center text-slate-500 text-xs font-semibold tracking-wider">
          <span>PREVENTED LOSS</span>
           <div className="w-5 h-5 rounded-full border border-emerald-200 text-emerald-500 flex items-center justify-center">
             <DollarSign size={12} />
          </div>
        </div>
        <div className="flex items-baseline gap-2">
          <span className="text-3xl font-bold text-slate-900">$1.2M</span>
          <span className="text-sm font-medium text-emerald-500">+18%</span>
        </div>
      </div>
    </div>
  )
}
