import { Link } from 'react-router-dom';
import { useDashboard } from '../../hooks/usePoints';

export function DashboardPage() {
  const { data, isLoading, error } = useDashboard();

  if (isLoading) return <DashboardSkeleton />;
  if (error) return <p className="text-red-600 text-sm p-4" role="alert">Failed to load dashboard.</p>;
  if (!data) return null;

  const { balance, tier, DunelmTierProgress, recentTransactions, activePromotions } = data;

  return (
    <div className="flex flex-col gap-6">
      {/* Points Balance Hero */}
      <section className="bg-gradient-to-br from-primary-black to-[#1e3a6e] rounded-sm p-8 text-center text-white" aria-label="Points balance">
        <p className="text-xs uppercase tracking-wide text-neutral-300 mb-1">Your Points Balance</p>
        <p className="font-display text-5xl font-bold" data-testid="points-balance">
          {balance.availablePoints.toLocaleString()}
        </p>
        <p className="text-sm text-neutral-300 mt-1">
          worth £{balance.monetaryEquivalent.toFixed(2)} · {tier?.name ?? 'Member'} tier
        </p>

        {/* Tier Progress */}
        {DunelmTierProgress && (
          <div className="mt-4">
            <div className="w-full bg-white/15 rounded-full h-1.5">
              <div className="bg-[#007A7A] h-1.5 rounded-full transition-all" style={{ width: `${DunelmTierProgress.progressPercent}%` }} />
            </div>
            <p className="text-xs text-neutral-400 mt-2">
              {(DunelmTierProgress.pointsRequired - DunelmTierProgress.pointsEarned).toLocaleString()} pts to {DunelmTierProgress.DunelmTierName} · {DunelmTierProgress.DunelmTierName === 'Silver' ? 'Early Sale Access' : 'Extended Sale Access'}
            </p>
          </div>
        )}
      </section>

      {/* Scan at Till CTA */}
      <Link to="/qr" className="flex items-center justify-center gap-2 bg-[#007A7A] text-white rounded-sm py-3 font-display text-sm font-semibold hover:bg-[#006565] transition-colors" aria-label="Show QR code to scan at till">
        <span className="w-5 h-5 bg-white/20 rounded flex items-center justify-center text-xs">▣</span>
        Scan at till
      </Link>

      {/* Recent Activity */}
      <section aria-label="Recent activity">
        <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-neutral-500 mb-3">Recent activity</h2>
        {recentTransactions.length > 0 ? (
          <div className="flex flex-col divide-y divide-neutral-200">
            {recentTransactions.map((tx) => (
              <div key={tx.transactionId ?? tx.referenceId} className="flex items-center justify-between py-3">
                <div>
                  <p className="font-display text-sm text-primary-black">{tx.description ?? tx.type}</p>
                  <p className="text-xs text-neutral-400">{new Date(tx.createdAt).toLocaleDateString()}</p>
                </div>
                <p className={`font-display text-sm font-bold ${tx.points > 0 ? 'text-[#1A7A4A]' : 'text-red-600'}`}>
                  {tx.points > 0 ? '+' : ''}{tx.points} pts
                </p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-neutral-400">Your Dunelm purchase will appear here</p>
        )}
        <Link to="/history" className="block mt-3 font-display text-sm text-[#007A7A] underline">View all activity</Link>
      </section>

      {/* Rewards Available */}
      <section className="bg-neutral-50 rounded-sm p-5" aria-label="Rewards available">
        <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-neutral-500 mb-2">Rewards available</h2>
        {balance.availablePoints >= 500 ? (
          <p className="text-sm text-neutral-700">You have <strong>£{balance.monetaryEquivalent.toFixed(2)}</strong> in rewards ready to use at checkout!</p>
        ) : (
          <p className="text-sm text-neutral-700">You need {500 - balance.availablePoints} more points to unlock your first £5 reward</p>
        )}
      </section>

      {/* Active Promotions */}
      {activePromotions.length > 0 && (
        <section aria-label="Active promotions">
          <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-neutral-500 mb-3">Promotions</h2>
          {activePromotions.map((promo) => (
            <div key={promo.id} className="bg-[#FF6A3B]/10 border border-[#FF6A3B]/30 rounded-sm p-4">
              <p className="font-display text-sm font-bold text-primary-black">{promo.name}</p>
              <p className="text-xs text-neutral-600 mt-1">{promo.description}</p>
            </div>
          ))}
        </section>
      )}
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div className="flex flex-col gap-6 animate-pulse">
      <div className="bg-neutral-200 rounded-sm h-48" />
      <div className="bg-neutral-200 rounded-sm h-12" />
      <div className="bg-neutral-200 rounded-sm h-32" />
      <div className="bg-neutral-200 rounded-sm h-20" />
    </div>
  );
}
