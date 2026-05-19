import React from 'react';
import { View, FlatList, StyleSheet, Pressable } from 'react-native';
import { Text } from 'react-native-paper';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../../lib/api-client';
import { colors, spacing } from '../../theme/tokens';

interface Notification {
  notificationId: string;
  title: string;
  body: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export function NotificationsScreen() {
  const queryClient = useQueryClient();
  const { data, isLoading } = useQuery<Notification[]>({
    queryKey: ['notifications'],
    queryFn: async () => (await apiClient.get('/customers/me/notifications')).data.data,
  });

  const markRead = useMutation({
    mutationFn: (id: string) => apiClient.put(`/customers/me/notifications/${id}/read`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const renderItem = ({ item }: { item: Notification }) => (
    <Pressable
      style={[styles.item, !item.read && styles.unread]}
      onPress={() => markRead.mutate(item.notificationId)}
      accessibilityLabel={`${item.title}. ${item.body}`}
    >
      <Text variant="titleMedium">{item.title}</Text>
      <Text variant="bodyMedium" style={styles.body}>{item.body}</Text>
      <Text variant="bodyMedium" style={styles.date}>{new Date(item.createdAt).toLocaleDateString()}</Text>
    </Pressable>
  );

  return (
    <View style={styles.container}>
      <Text variant="displayMedium" style={styles.title}>Notifications</Text>
      <FlatList
        data={data ?? []}
        keyExtractor={(item) => item.notificationId}
        renderItem={renderItem}
        refreshing={isLoading}
        ListEmptyComponent={<Text variant="bodyLarge" style={styles.empty}>No notifications yet</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.primary.white, paddingTop: spacing[12] },
  title: { paddingHorizontal: spacing[4], marginBottom: spacing[4] },
  item: { paddingHorizontal: spacing[4], paddingVertical: spacing[3], borderBottomWidth: 1, borderBottomColor: colors.neutral[200] },
  unread: { backgroundColor: colors.neutral[50] },
  body: { color: colors.neutral[600], marginTop: spacing[1] },
  date: { color: colors.neutral[400], marginTop: spacing[1] },
  empty: { padding: spacing[6], textAlign: 'center', color: colors.neutral[400] },
});
