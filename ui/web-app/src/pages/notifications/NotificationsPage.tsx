import { useNotifications, useMarkRead } from '../../hooks/useNotifications';

export function NotificationsPage() {
  const { data, isLoading } = useNotifications();
  const { mutate: markRead } = useMarkRead();

  if (isLoading) return <p className="font-serif text-sm text-neutral-500">Loading...</p>;

  const notifications = data?.data ?? [];

  return (
    <div className="flex flex-col gap-6">
      <h1 className="font-display text-3xl font-bold text-primary-black">Notifications</h1>

      {notifications.length === 0 ? (
        <p className="font-serif text-sm text-neutral-500">No notifications yet.</p>
      ) : (
        <ul className="divide-y divide-neutral-200">
          {notifications.map((n) => (
            <li
              key={n.notificationId}
              className={`py-4 flex items-start gap-3 ${!n.read ? 'bg-neutral-50' : ''}`}
            >
              {!n.read && <span className="mt-1.5 h-2 w-2 rounded-full bg-accent-teal flex-shrink-0" />}
              <div className="flex-1">
                <p className="font-display text-sm font-medium text-primary-black">{n.title}</p>
                <p className="font-serif text-sm text-neutral-600 mt-1">{n.body}</p>
                <p className="font-display text-xs text-neutral-400 mt-1">
                  {new Date(n.createdAt).toLocaleString()}
                </p>
              </div>
              {!n.read && (
                <button
                  onClick={() => markRead(n.notificationId)}
                  className="font-display text-xs text-accent-teal underline min-h-[44px] flex items-center"
                >
                  Mark read
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
