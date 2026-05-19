import React from 'react';
import { View, Text, FlatList, StyleSheet } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../../lib/api-client';
import { tokens } from '../../theme/tokens';

interface Transaction {
  id: string;
  type: 'earn' | 'redeem' | 'adjust' | 'bonus' | 'expire' | 'reverse';
  points: number;
  referenceId: string;
  channel: string;
  createdAt: string;
}

export function HistoryScreen() {
  const { data, isLoading } = useQuery({
    queryKey: ['transactions'],
    queryFn: () => apiClient.get('/api/v1/points/transactions?pageSize=20').then(r => r.data),
  });

  const renderItem = ({ item }: { item: Transaction }) => (
    <View style={styles.item} accessibilityRole="listitem">
      <View style={styles.row}>
        <Text style={styles.type}>{item.type.toUpperCase()}</Text>
        <Text style={[styles.points, item.points > 0 ? styles.positive : styles.negative]}>
          {item.points > 0 ? '+' : ''}{item.points} pts
        </Text>
      </View>
      <Text style={styles.meta}>{item.channel} • {new Date(item.createdAt).toLocaleDateString()}</Text>
    </View>
  );

  if (isLoading) {
    return <View style={styles.container}><Text>Loading...</Text></View>;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Transaction History</Text>
      <FlatList
        data={data?.data || []}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        ListEmptyComponent={<Text style={styles.empty}>No transactions yet. Make a purchase to start earning!</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: tokens.colors.white, padding: 16 },
  title: { fontSize: 22, fontWeight: '700', color: tokens.colors.black, marginBottom: 16 },
  item: { paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: tokens.colors.neutral200 },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  type: { fontSize: 11, fontWeight: '600', color: tokens.colors.neutral500, letterSpacing: 0.5 },
  points: { fontSize: 16, fontWeight: '700' },
  positive: { color: tokens.colors.success },
  negative: { color: tokens.colors.error },
  meta: { fontSize: 12, color: tokens.colors.neutral400, marginTop: 4 },
  empty: { fontSize: 14, color: tokens.colors.neutral500, textAlign: 'center', marginTop: 40 },
});
