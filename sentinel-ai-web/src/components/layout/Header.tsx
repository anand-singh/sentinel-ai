import Link from 'next/link'
import { Search, Bell, ShieldCheck } from 'lucide-react'

const navLinks = [
  { href: '/', label: 'Dashboard' },
  { href: '/analytics', label: 'Analytics' },
  { href: '/admin', label: 'Admin' },
]

export function Header() {
  return (
    <header className="bg-[#1e293b] flex flex-col text-white shrink-0">
      {/* Top bar */}
      <div className="h-14 flex items-center px-6 justify-between">
        <div className="flex items-center gap-3">
          <div className="bg-blue-500 p-1.5 rounded-md flex items-center justify-center">
            <ShieldCheck size={20} className="text-white" />
          </div>
          <div className="font-semibold text-lg flex items-center gap-2">
            Sentinel <span className="text-slate-400 font-normal">|</span>{' '}
            <span className="font-normal text-slate-300">Case Manager</span>
          </div>
        </div>

        <div className="flex-1 max-w-2xl mx-8">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
            <input
              type="text"
              placeholder="Search Case ID, Customer, or IP Address..."
              className="w-full bg-[#334155] border-none rounded-md py-1.5 pl-10 pr-4 text-sm text-white placeholder:text-slate-400 focus:outline-none focus:ring-1 focus:ring-slate-400"
            />
          </div>
        </div>

        <div className="flex items-center gap-6">
          <div className="relative">
            <Bell size={20} className="text-slate-300" />
            <div className="absolute -top-1 -right-1 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-[#1e293b]"></div>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex flex-col items-end">
              <span className="text-sm font-medium">Marcus Vance</span>
              <span className="text-xs text-slate-400">Sr. Financial Analyst</span>
            </div>
            <div className="w-8 h-8 rounded-full bg-orange-200 overflow-hidden flex items-end justify-center pt-2">
              <div className="w-4 h-4 bg-orange-800 rounded-t-full"></div>
            </div>
          </div>
        </div>
      </div>

      {/* Nav bar */}
      <div className="flex items-center gap-1 px-6 border-t border-slate-700/60 bg-[#1a2332]">
        {navLinks.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className="text-xs font-bold tracking-wider text-slate-400 hover:text-white hover:bg-white/10 px-4 py-2.5 rounded-sm transition-colors uppercase"
          >
            {link.label}
          </Link>
        ))}
      </div>
    </header>
  )
}
