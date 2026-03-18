export function CaseRiskScore() {
  return (
    <div className="bg-white border text-center border-slate-200 rounded-lg p-6 shadow-sm flex flex-col items-center justify-center h-full">
      <div className="w-full text-left bg-white text-sm font-bold tracking-widest text-[#1e293b] mb-6">
        CASE RISK SCORE: #F-9204
      </div>
      
      <div className="relative w-48 h-48 mt-4 flex items-center justify-center">
        {/* SVG Circle visualizer */}
        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
          <circle
            cx="50"
            cy="50"
            r="40"
            fill="transparent"
            stroke="#f1f5f9"
            strokeWidth="8"
          />
          {/* Dasharray represents 85% of circumference (2 * pi * 40 = ~251. 85% of 251 = 213) */}
          <circle
            cx="50"
            cy="50"
            r="40"
            fill="transparent"
            stroke="#ef4444" 
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray="213 251"
            strokeDashoffset="0"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-4xl font-bold text-red-500">85</span>
          <span className="text-[10px] font-bold tracking-widest text-slate-400 mt-1">VERY HIGH</span>
        </div>
      </div>

      <div className="mt-8 text-sm text-slate-500 max-w-xs text-center leading-relaxed font-medium">
        This transaction exceeds standard risk thresholds due to multiple failure signals.
      </div>
    </div>
  )
}
