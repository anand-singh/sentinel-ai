import { Badge } from "@/components/ui/badge"
import { LinkedEntity } from "@/types/case"
import { Link2 } from "lucide-react"

interface EntityLinkingProps {
  entities?: LinkedEntity[]
}

export function EntityLinking({ entities = [] }: EntityLinkingProps) {
  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-zinc-900">Entity Linking</h2>
      <div className="bg-white p-6 rounded-lg border">
        {entities.length === 0 ? (
          <div className="text-center text-zinc-500 py-8">No linked entities identified.</div>
        ) : (
          <div className="relative">
            <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-zinc-100" />
            <div className="space-y-6 relative">
              {entities.map((e) => (
                <div key={e.id} className="flex items-start gap-4">
                  <div className="z-10 bg-white p-1 rounded-full border border-zinc-200 mt-1">
                    <Link2 className="h-4 w-4 text-zinc-400" />
                  </div>
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center justify-between">
                      <div className="font-mono text-sm font-semibold text-zinc-900">{e.id}</div>
                      <Badge variant={e.riskLevel === 'low' ? 'secondary' : e.riskLevel === 'medium' ? 'default' : 'destructive'}>
                        {e.riskLevel.toUpperCase()}
                      </Badge>
                    </div>
                    <div className="text-xs text-zinc-500">
                      Type: <span className="capitalize">{e.type}</span> • Relationship: <span className="text-zinc-700 font-medium">{e.relationship}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
