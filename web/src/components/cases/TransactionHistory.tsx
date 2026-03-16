import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Transaction } from "@/types/case"

interface TransactionHistoryProps {
  transactions?: Transaction[]
}

export function TransactionHistory({ transactions = [] }: TransactionHistoryProps) {
  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-zinc-900">Transaction History</h2>
      <div className="rounded-md border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date</TableHead>
              <TableHead>Merchant</TableHead>
              <TableHead>Amount</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Transaction ID</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {transactions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="h-24 text-center text-zinc-500">
                  No transactions found.
                </TableCell>
              </TableRow>
            ) : (
              transactions.map((t) => (
                <TableRow key={t.id}>
                  <TableCell className="text-sm text-zinc-600">
                    {new Date(t.date).toLocaleString()}
                  </TableCell>
                  <TableCell className="font-medium text-zinc-900">{t.merchant}</TableCell>
                  <TableCell className="font-mono text-sm">${t.amount.toLocaleString()}</TableCell>
                  <TableCell>
                    <Badge variant={t.status === 'completed' ? 'secondary' : 'destructive'}>
                      {t.status.toUpperCase()}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right font-mono text-xs text-zinc-500">{t.id}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
