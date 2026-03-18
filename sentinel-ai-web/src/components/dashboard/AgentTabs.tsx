'use client'

import { useState } from 'react'
import { AgentOutput } from '@/types/alert'

interface AgentTabsProps {
  outputs: AgentOutput[]
}

export function AgentTabs({ outputs }: AgentTabsProps) {
  const [activeTab, setActiveTab] = useState(0)
  const active = outputs[activeTab]

  const agentColors: Record<string, string> = {
    'Pattern Analyzer': 'text-blue-600 border-blue-500',
    'Behavioral Risk Agent': 'text-purple-600 border-purple-500',
    'Evidence Builder': 'text-amber-600 border-amber-500',
    'Aggregated Risk Scorer': 'text-red-600 border-red-500',
  }

  return (
    <div className="bg-white border border-slate-200 rounded-lg shadow-sm overflow-hidden">
      {/* Tab Headers */}
      <div className="flex border-b border-slate-200 bg-slate-50 overflow-x-auto">
        {outputs.map((o, i) => (
          <button
            key={o.agentName}
            onClick={() => setActiveTab(i)}
            className={`shrink-0 px-5 py-3.5 text-xs font-bold tracking-wider transition-all border-b-2 ${
              activeTab === i
                ? `bg-white ${agentColors[o.agentName] ?? 'text-slate-800 border-slate-800'}`
                : 'text-slate-400 border-transparent hover:text-slate-600 hover:bg-white/60'
            }`}
          >
            {o.agentName.toUpperCase()}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {active && (
        <div className="p-6">
          <p className="text-sm text-slate-700 leading-relaxed mb-5">{active.summary}</p>
          <div className="flex flex-wrap gap-2">
            {active.flags.map((flag) => (
              <span
                key={flag}
                className="text-[10px] font-bold tracking-wider px-2.5 py-1 rounded bg-red-50 text-red-700 border border-red-200 font-mono"
              >
                {flag}
              </span>
            ))}
          </div>
          <div className="mt-4 text-[10px] text-slate-400 font-medium">
            Processed at {new Date(active.timestamp).toLocaleTimeString()}
          </div>
        </div>
      )}
    </div>
  )
}
