import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { DashboardScreen } from './DashboardScreen';

jest.mock('../../lib/api-client', () => ({
  apiClient: {
    get: jest.fn().mockResolvedValue({
      data: {
        data: {
          balance: { availablePoints: 2500, monetaryEquivalent: 125.0, pendingPoints: 0, tierMultiplier: 1.5 },
          tier: { name: 'Gold', badgeColor: '#FFD700', benefits: [] },
          DunelmTierProgress: { DunelmTierName: 'Platinum', pointsRequired: 10000, pointsEarned: 5200, progressPercent: 52 },
          recentTransactions: [
            { transactionId: '1', type: 'earn', points: 76, description: 'Online purchase', createdAt: '2026-05-18T14:30:00Z' },
          ],
        },
      },
    }),
  },
}));

jest.mock('../../hooks/useAuth', () => ({
  useNetInfo: () => ({ isConnected: true }),
}));

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
}

describe('DashboardScreen', () => {
  it('renders balance value', async () => {
    const { getByTestId } = renderWithProviders(<DashboardScreen />);
    await waitFor(() => {
      expect(getByTestId('balance-value')).toBeTruthy();
    });
  });

  it('renders tier name', async () => {
    const { getByText } = renderWithProviders(<DashboardScreen />);
    await waitFor(() => {
      expect(getByText('Gold Member')).toBeTruthy();
    });
  });
});
