export default function AdminPage() {
  const config = {
    alertRouting: {
      criticalThreshold: 80,
      highThreshold: 60,
      mediumThreshold: 40,
      autoFreezeAbove: 85,
      autoNotifySecurityAbove: 70,
    },
    agents: [
      { name: 'Pattern Analyzer', version: 'v2.4.1', status: 'ACTIVE', lastDeployed: '2026-03-10' },
      { name: 'Behavioral Risk Agent', version: 'v1.9.3', status: 'ACTIVE', lastDeployed: '2026-03-08' },
      { name: 'Evidence Builder', version: 'v1.5.0', status: 'ACTIVE', lastDeployed: '2026-03-01' },
      { name: 'Aggregated Risk Scorer', version: 'v3.0.2', status: 'ACTIVE', lastDeployed: '2026-03-12' },
      { name: 'Action Executor', version: 'v2.1.0', status: 'ACTIVE', lastDeployed: '2026-03-12' },
    ],
    policies: [
      { id: 'POL-001', name: 'Freeze Threshold Policy', version: 'v3.1.2', updatedAt: '2026-03-14', scope: 'All Transactions' },
      { id: 'POL-002', name: 'Velocity Check Policy', version: 'v2.0.1', updatedAt: '2026-02-28', scope: 'Digital Wallet' },
      { id: 'POL-003', name: 'Geo-Mismatch Policy', version: 'v1.4.0', updatedAt: '2026-03-01', scope: 'International' },
      { id: 'POL-004', name: 'AML Screening Policy', version: 'v4.0.0', updatedAt: '2026-03-10', scope: 'Wire Transfer' },
    ],
    retentionDays: 90,
    auditLogImmutable: true,
    piiMaskingEnabled: true,
  }

  return (
    <div className="max-w-[1400px] mx-auto flex flex-col gap-4">
      {/* Header */}
      <div className="mb-2">
        <h1 className="text-xl font-bold text-slate-900">Admin / Policy Viewer</h1>
        <p className="text-sm text-slate-500 mt-1">Read-only view of system configuration, agent versions, and routing policies</p>
      </div>

      {/* System Flags */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'PII Masking', value: config.piiMaskingEnabled ? 'Enabled' : 'Disabled', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' },
          { label: 'Audit Log', value: config.auditLogImmutable ? 'Immutable' : 'Mutable', color: 'text-blue-700 bg-blue-50 border-blue-200' },
          { label: 'Data Retention', value: `${config.retentionDays} days`, color: 'text-slate-700 bg-slate-50 border-slate-200' },
        ].map((item) => (
          <div key={item.label} className="bg-white border border-slate-200 rounded-lg p-5 shadow-sm flex flex-col gap-2">
            <div className="text-xs font-bold tracking-widest text-slate-400 uppercase">{item.label}</div>
            <span className={`text-sm font-bold px-2.5 py-1 rounded border self-start ${item.color}`}>{item.value}</span>
          </div>
        ))}
      </div>

      {/* Alert Routing Config */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-sm font-bold tracking-widest text-[#1e293b] uppercase mb-5">Alert Routing Configuration</h2>
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
          {Object.entries(config.alertRouting).map(([key, val]) => (
            <div key={key} className="flex flex-col gap-1">
              <div className="text-[10px] font-bold tracking-wider text-slate-400 uppercase">
                {key.replace(/([A-Z])/g, ' $1').trim()}
              </div>
              <div className="text-lg font-bold font-mono text-slate-900">{val}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Agent Versions */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-sm font-bold tracking-widest text-[#1e293b] uppercase mb-5">Agent Versions</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-[10px] font-bold tracking-widest text-slate-400 uppercase border-b border-slate-100">
              <th className="text-left pb-3">Agent</th>
              <th className="text-left pb-3">Version</th>
              <th className="text-left pb-3">Status</th>
              <th className="text-left pb-3">Last Deployed</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {config.agents.map((agent) => (
              <tr key={agent.name} className="hover:bg-slate-50 transition-colors">
                <td className="py-3 font-semibold text-slate-800">{agent.name}</td>
                <td className="py-3 font-mono text-xs text-blue-700 font-bold">{agent.version}</td>
                <td className="py-3">
                  <span className="text-[10px] font-bold tracking-wider px-2.5 py-1 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                    {agent.status}
                  </span>
                </td>
                <td className="py-3 text-slate-500 text-xs">{agent.lastDeployed}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Policy Versions */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-sm font-bold tracking-widest text-[#1e293b] uppercase mb-5">Policy Versions</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-[10px] font-bold tracking-widest text-slate-400 uppercase border-b border-slate-100">
              <th className="text-left pb-3">Policy ID</th>
              <th className="text-left pb-3">Name</th>
              <th className="text-left pb-3">Version</th>
              <th className="text-left pb-3">Scope</th>
              <th className="text-left pb-3">Updated</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {config.policies.map((pol) => (
              <tr key={pol.id} className="hover:bg-slate-50 transition-colors">
                <td className="py-3 font-mono text-xs text-slate-500">{pol.id}</td>
                <td className="py-3 font-semibold text-slate-800">{pol.name}</td>
                <td className="py-3 font-mono text-xs text-emerald-700 font-bold">{pol.version}</td>
                <td className="py-3 text-slate-500 text-xs">{pol.scope}</td>
                <td className="py-3 text-slate-500 text-xs">{pol.updatedAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Footer */}
      <div className="flex justify-between items-center text-[10px] font-bold tracking-widest text-[#94a3b8] py-4 border-t border-slate-200 mt-2 uppercase">
        <span>&copy; 2026 Sentinel Case Management System</span>
        <div className="flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
          <span>ALL SYSTEMS OPERATIONAL</span>
        </div>
      </div>
    </div>
  )
}
