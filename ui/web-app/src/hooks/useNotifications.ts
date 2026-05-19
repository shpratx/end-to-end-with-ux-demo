import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/api-client';

interface Notification {
  notificationId: string;
  title: string;
  body: string;
  type: string;
  read: boolean;
  createdAt: string;
}

interface PaginatedNotifications {
  data: Notification[];
  meta: { pageNumber: number; pageSize: number; totalItems: number; totalPages: number };
}

export function useNotifications(page = 1) {
  return useQuery<PaginatedNotifications>({
    queryKey: ['notifications', page],
    queryFn: () =>
      apiClient.get<PaginatedNotifications>('/notifications', { params: { pageNumber: page } }).then((r) => r.data),
  });
}

export function useUnreadCount() {
  return useQuery<number>({
    queryKey: ['notifications', 'unread-count'],
    queryFn: () =>
      apiClient.get<{ unreadCount: number }>('/notifications/unread-count').then((r) => r.data.unreadCount),
  });
}

export function useMarkRead() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: async (id) => {
      await apiClient.patch(`/notifications/${id}/read`, null, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      });
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}
