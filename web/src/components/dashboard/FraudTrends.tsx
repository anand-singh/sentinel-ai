export function FraudTrends() {
  // Simple randomized heights to mock a bar chart
  const bars = [
    { height: '40%', color: 'bg-slate-200' },
    { height: '55%', color: 'bg-slate-200' },
    { height: '30%', color: 'bg-slate-200' },
    { height: '65%', color: 'bg-blue-400' },
    { height: '80%', color: 'bg-blue-400' },
    { height: '100%', color: 'bg-red-500' },
    { height: '60%', color: 'bg-blue-400' },
    { height: '45%', color: 'bg-slate-200' },
    { height: '35%', color: 'bg-slate-200' },
    { height: '50%', color: 'bg-slate-200' },
  ]

  return (
    <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm flex flex-col h-full">
      <div className="flex justify-between items-center mb-8">
        <h3 className="text-sm font-bold tracking-widest text-[#1e293b]">FRAUD TRENDS OVER TIME</h3>
        <button className="text-xs font-semibold text-slate-500 bg-slate-50 px-3 py-1.5 rounded-full border border-slate-200">
          Last 30 Days
        </button>
      </div>
      
      <div className="flex-1 flex items-end justify-between px-2 gap-4 h-48">
         {bars.map((bar, i) => (
           <div 
             key={i} 
             className={`w-full max-w-[40px] rounded-t-sm ${bar.color}`}
             style={{ height: bar.height }}
           />
         ))}
      </div>

      <div className="flex justify-between text-[10px] font-bold text-slate-400 tracking-wider mt-4 px-2">
        <span>May 01</span>
        <span>May 07</span>
        <span>May 14</span>
        <span>May 21</span>
        <span>May 30</span>
      </div>
    </div>
  )
}
