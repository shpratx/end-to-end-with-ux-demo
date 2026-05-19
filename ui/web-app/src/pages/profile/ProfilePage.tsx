import { useAuthStore } from '../../lib/auth-store';
import { useLogout } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';

export function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const { mutate: logout } = useLogout();

  return (
    <div className="flex flex-col gap-6 max-w-[640px]">
      <h1 className="font-display text-3xl font-bold text-primary-black">Profile</h1>
      {user && (
        <div className="rounded-sm border border-neutral-200 p-6 shadow-xs">
          <p className="font-display text-sm text-neutral-500">Name</p>
          <p className="font-display text-md font-medium text-primary-black">{user.name}</p>
          <p className="font-display text-sm text-neutral-500 mt-4">Email</p>
          <p className="font-display text-md font-medium text-primary-black">{user.email}</p>
          <p className="font-display text-sm text-neutral-500 mt-4">Tier</p>
          <p className="font-display text-md font-medium text-primary-black">{user.tier}</p>
        </div>
      )}
      <Button variant="danger" onClick={() => logout()}>Sign Out</Button>
    </div>
  );
}
