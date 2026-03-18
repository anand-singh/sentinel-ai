import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Case, RiskLevel } from "@/types/case"
import Link from "next/link"

interface CaseTableProps {
  cases: Case[]
}

const riskLevelMap: Record<RiskLevel, { label: string, variant: "default" | "secondary" | "destructive" | "outline" }> = {
  low: { label: "Low", variant: "secondary" },
  medium: { label: "Medium", variant: "default" },
  high: { label: "High", variant: "destructive" },
  critical: { label: "Critical", variant: "destructive" },
}

export function CaseTable({ cases }: CaseTableProps) {
  return (
    <div className="rounded-md border bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[120px]">ID</TableHead>
            <TableHead>Customer</TableHead>
            <TableHead>Risk Level</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Amount</TableHead>
            <TableHead className="text-right">Date</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {cases.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="h-24 text-center text-zinc-500">
                No cases found.
              </TableCell>
            </TableRow>
          ) : (
            cases.map((c) => (
              <TableRow key={c.id}>
                <TableCell className="font-mono text-xs">
                  <Link href={`/cases/${c.id}`} className="hover:underline text-blue-600">
                    {c.id}
                  </Link>
                </TableCell>
                <TableCell className="font-medium text-zinc-900">{c.customerName}</TableCell>
                <TableCell>
                  <Badge variant={riskLevelMap[c.riskLevel].variant}>
                    {riskLevelMap[c.riskLevel].label}
                  </Badge>
                </TableCell>
                <TableCell className="capitalize text-zinc-600 text-sm">
                  {c.status.replace('_', ' ')}
                </TableCell>
                <TableCell className="font-mono text-sm">${c.amount.toLocaleString()}</TableCell>
                <TableCell className="text-right text-zinc-500 text-xs">
                  {new Date(c.date).toLocaleDateString()}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  )
}
