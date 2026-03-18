import Link from 'next/link'

export function Sidebar() {
  return (
    <aside className="w-64 bg-zinc-900 text-white h-screen flex flex-col">
      <div className="p-6 font-bold text-xl border-b border-zinc-800">
        Sentinel AI
      </div>
      <nav className="flex-1 p-4 space-y-2">
        <Link href="/" className="block p-2 hover:bg-zinc-800 rounded">Dashboard</Link>
        <Link href="/cases" className="block p-2 hover:bg-zinc-800 rounded">Cases</Link>
        <Link href="/analytics" className="block p-2 hover:bg-zinc-800 rounded">Analytics</Link>
      </nav>
    </aside>
  )
}
