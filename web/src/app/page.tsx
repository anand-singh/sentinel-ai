import { SummaryCards } from '@/components/dashboard/SummaryCards'
import { CaseRiskScore } from '@/components/dashboard/CaseRiskScore'
import { FraudTrends } from '@/components/dashboard/FraudTrends'
import { ReasonChain } from '@/components/dashboard/ReasonChain'
import { RecentAlerts } from '@/components/dashboard/RecentAlerts'
import { IngestTransactionModal } from '@/components/dashboard/IngestTransactionModal'

export default function Home() {
  return (
    <div className="max-w-[1400px] mx-auto flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <SummaryCards />
        <div className="shrink-0 pl-4">
          <IngestTransactionModal />
        </div>
      </div>
      
      <div className="grid grid-cols-12 gap-4">
        {/* Left Column (Approx 3.5/12 width) */}
        <div className="col-span-12 xl:col-span-4 flex flex-col gap-4">
           {/* Setting heights to match visual proportion */}
           <div className="min-h-[400px]">
             <CaseRiskScore />
           </div>
           <div className="flex-1 min-h-[400px] mb-4">
             <ReasonChain />
           </div>
        </div>
        
        {/* Right Column (Approx 8.5/12 width) */}
        <div className="col-span-12 xl:col-span-8 flex flex-col gap-4">
           <div className="min-h-[400px]">
             <FraudTrends />
           </div>
           <div className="flex-1 min-h-[400px] mb-4">
             <RecentAlerts />
           </div>
        </div>
      </div>
      
      {/* Footer System Status Bar Mock */}
      <div className="flex justify-between items-center text-[10px] font-bold tracking-widest text-[#94a3b8] py-4 border-t border-slate-200 mt-2 uppercase">
         <div className="flex gap-4">
            <span>&copy; 2026 Sentinel Case Management System</span>
            <span className="hover:text-slate-500 cursor-pointer transition-colors">Compliance & Security</span>
            <span className="hover:text-slate-500 cursor-pointer transition-colors">System Logs</span>
         </div>
         <div className="flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
            <span>ALL SYSTEMS OPERATIONAL</span>
         </div>
      </div>
    </div>
  )
}
