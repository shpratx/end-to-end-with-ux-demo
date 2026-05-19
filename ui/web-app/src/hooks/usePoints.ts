import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../lib/api-client';

interface PointsBalance {
  availablePoints: number;
  monetaryEquivalent: number;
  pendingPoints: number;
  tierMultiplier: number;
  lastUpdated: string;
}

interface Transaction {
  transactionId: string;
  type: string;
  points: number;
  runningBalance: number;
  referenceId: string;
  channel: string;
  description: string;
  createdAt: string;
}

interface Tier {
  tierId: string;
  name: string;
  threshold: number;
  earnRateMultiplier: number;
  badgeColor: string;
  benefits: string[];
}

interface NextTierProgress {
  nextTierName: string;
  pointsRequired: number;
  pointsEarned: number;
  progressPercent: number;
}

interface Promotion {
  id: string;
  name: string;
  description: string;
  endsAt: string;
}

interface DashboardData {
  balance: PointsBalance;
  tier: Tier;
  nextTierProgress: NextTierProgress | null;
  recentTransactions: Transaction[];
  activePromotions: Promotion[];
}

interface PaginatedResponse<T> {
  data: T[];
  meta: { pageNumber: number; pageSize: number; totalItems: number; totalPages: number };
}

export function useBalance() {
  return useQuery<PointsBalance>({
    queryKey: ['points', 'balance'],
    queryFn: () => apiClient.get<PointsBalance>('/points/balance').then((r) => r.data),
  });
}

export function useTransactions(page = 1) {
  return useQuery<PaginatedResponse<Transaction>>({
    queryKey: ['points', 'transactions', page],
    queryFn: () =>
      apiClient.get<PaginatedResponse<Transaction>>('/points/transactions', { params: { pageNumber: page } }).then((r) => r.data),
  });
}

export function useDashboard() {
  return useQuery<DashboardData>({
    queryKey: ['dashboard'],
    queryFn: () =>
      apiClient.get<{ data: DashboardData }>('/customers/me/dashboard').then((r) => r.data.data),
  });
}
