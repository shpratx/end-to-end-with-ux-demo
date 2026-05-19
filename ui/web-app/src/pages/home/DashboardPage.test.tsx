import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from './DashboardPage';

const mockDashboard = {
  balance: { availablePoints: 2500, monetaryEquivalent: 125.0, pendingPoints: 0, tierMultiplier: 1.5, lastUpdated: '2026-05-18T11:45:00Z' },
  tier: { tierId: '1', name: 'Gold', threshold: 5000, earnRateMultiplier: 1.5, badgeColor: '#FFD700', benefits: [] },
  DunelmTierProgress: { DunelmTierName: 'Platinum', pointsRequired: 10000, pointsEarned: 5200, progressPercent: 52 },
  recentTransactions: [
    { transactionId: 'tx1', type: 'earn', points: 76, runningBalance: 2576, referenceId: 'ORD-001', channel: 'online', description: 'Online purchase', createdAt: '2026-05-18T14:30:00Z' },
  ],
  activePromotions: [
    { id: 'p1', name: 'Summer Double Points', description: '2x points on all purchases', endsAt: '2026-06-30T23:59:59Z' },
  ],
};

vi.mock('../../hooks/usePoints', () => ({
  useDashboard: () => ({ data: mockDashboard, isLoading: false, error: null }),
}));

function renderPage() {
  const qc = new QueryClient();
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('DashboardPage', () => {
  it('renders points balance', () => {
    renderPage();
    expect(screen.getByTestId('points-balance')).toHaveTextContent('2,500');
  });

  it('renders current tier', () => {
    renderPage();
    expect(screen.getByTestId('current-tier')).toHaveTextContent('Gold');
  });

  it('renders recent transactions', () => {
    renderPage();
    expect(screen.getByText('Online purchase')).toBeInTheDocument();
    expect(screen.getByText('+76')).toBeInTheDocument();
  });

  it('renders active promotions', () => {
    renderPage();
    expect(screen.getByText('Summer Double Points')).toBeInTheDocument();
  });
});
