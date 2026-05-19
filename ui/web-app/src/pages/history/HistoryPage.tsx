import { useTransactions } from '../../hooks/usePoints';

export function HistoryPage() {
  const { data, isLoading } = useTransactions();

  if (isLoading) return <p className="font-serif text-sm text-neutral-500">Loading...</p>;

  const transactions = data?.data ?? [];

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-display text-3xl font-bold text-primary-black">Transaction History</h1>
      {transactions.length === 0 ? (
        <p className="font-serif text-sm text-neutral-500">No transactions yet.</p>
      ) : (
        <ul className="divide-y divide-neutral-200">
          {transactions.map((tx) => (
            <li key={tx.transactionId} className="flex items-center justify-between py-4">
              <div>
                <p className="font-display text-sm font-medium text-primary-black">{tx.description}</p>
                <p className="font-display text-xs text-neutral-400">{tx.channel} · {new Date(tx.createdAt).toLocaleDateString()}</p>
              </div>
              <span className={`font-display text-sm font-bold ${tx.points >= 0 ? 'text-success' : 'text-error'}`}>
                {tx.points >= 0 ? '+' : ''}{tx.points}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
