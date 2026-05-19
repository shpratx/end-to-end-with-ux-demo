import { type ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../lib/auth-store';

interface AppShellProps {
  children: ReactNode;
}

const navItems = [
  { path: '/home', label: 'Home' },
  { path: '/history', label: 'History' },
  { path: '/qr', label: 'QR Code' },
  { path: '/notifications', label: 'Notifications' },
  { path: '/profile', label: 'Profile' },
];

export function AppShell({ children }: AppShellProps) {
  const location = useLocation();
  const user = useAuthStore((s) => s.user);

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-[1000] h-16 bg-primary-black shadow-sm">
        <div className="mx-auto flex h-full max-w-[1440px] items-center justify-between px-6">
          <Link to="/home" className="font-display text-lg font-bold text-primary-white uppercase tracking-wide">
            NEXT Loyalty
          </Link>
          <nav className="hidden md:flex items-center gap-6">
            {navItems.map(({ path, label }) => (
              <Link
                key={path}
                to={path}
                className={`font-display text-sm font-medium uppercase tracking-wide transition-colors min-h-[44px] flex items-center ${
                  location.pathname === path
                    ? 'text-primary-white border-b-2 border-accent-teal'
                    : 'text-neutral-300 hover:text-primary-white'
                }`}
              >
                {label}
              </Link>
            ))}
          </nav>
          {user && (
            <span className="font-display text-sm text-neutral-300 hidden lg:block">
              {user.name}
            </span>
          )}
        </div>
      </header>

      <main className="flex-1 mx-auto w-full max-w-[1440px] px-4 py-8 md:px-6">
        {children}
      </main>

      <footer className="border-t border-neutral-200 py-6">
        <div className="mx-auto max-w-[1440px] px-6 text-center">
          <p className="font-display text-xs text-neutral-500">
            © {new Date().getFullYear()} Next plc. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
