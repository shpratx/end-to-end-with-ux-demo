import React from 'react';
import { View, ScrollView, StyleSheet, RefreshControl } from 'react-native';
import { Text, Card } from 'react-native-paper';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../../lib/api-client';
import { colors, spacing, radii, shadows } from '../../theme/tokens';
import { useNetInfo } from '../../hooks/useAuth';

interface DashboardData {
  balance: { availablePoints: number; monetaryEquivalent: number; pendingPoints: number; tierMultiplier: number };
  tier: { name: string; badgeColor: string; benefits: string[] };
  nextTierProgress: { nextTierName: string; pointsRequired: number; pointsEarned: number; progressPercent: number };
  recentTransactions: Array<{ transactionId: string; type: string; points: number; description: string; createdAt: string }>;
}

export function DashboardScreen() {
  const { data, isLoading, refetch } = useQuery<DashboardData>({
    queryKey: ['dashboard'],
    queryFn: async () => (await apiClient.get('/customers/me/dashboard')).data.data,
  });

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor={colors.accent.teal} />}
    >
      <Text variant="displayMedium" style={styles.greeting}>Next Loyalty</Text>

      {/* Balance Card */}
      <View style={styles.balanceCard} accessibilityLabel={`Points balance: ${data?.balance.availablePoints ?? 0}`}>
        <Text style={styles.balanceLabel}>Available Points</Text>
        <Text style={styles.balanceValue} testID="balance-value">{data?.balance.availablePoints?.toLocaleString() ?? '—'}</Text>
        <Text style={styles.balanceMoney}>Worth £{data?.balance.monetaryEquivalent?.toFixed(2) ?? '0.00'}</Text>
      </View>

      {/* Tier */}
      {data?.tier && (
        <Card style={styles.card}>
          <Card.Content>
            <Text variant="titleMedium">{data.tier.name} Member</Text>
            <Text variant="bodyMedium" style={styles.muted}>{data.nextTierProgress.progressPercent}% to {data.nextTierProgress.nextTierName}</Text>
          </Card.Content>
        </Card>
      )}

      {/* Recent Transactions */}
      <Text variant="titleLarge" style={styles.sectionTitle}>Recent Activity</Text>
      {data?.recentTransactions.map((tx) => (
        <View key={tx.transactionId} style={styles.txRow}>
          <View style={styles.txInfo}>
            <Text variant="bodyLarge">{tx.description}</Text>
            <Text variant="bodyMedium" style={styles.muted}>{new Date(tx.createdAt).toLocaleDateString()}</Text>
          </View>
          <Text style={[styles.txPoints, tx.points > 0 ? styles.positive : styles.negative]}>
            {tx.points > 0 ? '+' : ''}{tx.points}
          </Text>
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.neutral[50] },
  content: { padding: spacing[4], paddingTop: spacing[12] },
  greeting: { marginBottom: spacing[6] },
  balanceCard: {
    backgroundColor: colors.primary.black,
    borderRadius: radii.lg,
    padding: spacing[6],
    marginBottom: spacing[4],
    ...shadows.md,
  },
  balanceLabel: { color: colors.neutral[400], fontSize: 13, fontWeight: '500' },
  balanceValue: { color: colors.primary.white, fontSize: 43, fontWeight: '700', marginVertical: spacing[1] },
  balanceMoney: { color: colors.neutral[300], fontSize: 16 },
  card: { marginBottom: spacing[4] },
  sectionTitle: { marginTop: spacing[6], marginBottom: spacing[3] },
  txRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: spacing[3], borderBottomWidth: 1, borderBottomColor: colors.neutral[200] },
  txInfo: { flex: 1 },
  txPoints: { fontSize: 16, fontWeight: '700' },
  positive: { color: colors.semantic.success },
  negative: { color: colors.semantic.error },
  muted: { color: colors.neutral[500] },
});
