import { type ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../lib/auth-store';

interface AppShellProps {
  children: ReactNode;
}

const navItems = [
  { path: '/home', label: 'Home', icon: '⌂' },
  { path: '/history', label: 'History', icon: '◷' },
  { path: '/qr', label: 'QR', icon: '▣' },
  { path: '/notifications', label: 'Inbox', icon: '✉' },
  { path: '/profile', label: 'Profile', icon: '◌' },
];

export function AppShell({ children }: AppShellProps) {
  const location = useLocation();
  const user = useAuthStore((s) => s.user);

  return (
    <div className="flex h-full flex-col bg-dunelm-page">
      {/* Forest-green app header (Dunelm brand) */}
      <header className="sticky top-0 z-[900] bg-dunelm-forest pt-8 pb-3 px-5 shadow-sm">
        <div className="flex items-center justify-between">
          <Link
            to="/home"
            className="font-display text-xl font-bold text-white tracking-tight"
          >
            Dunelm
          </Link>
          {user && (
            <span className="font-serif text-xs text-white/75">
              Hi, {user.name.split(' ')[0]}
            </span>
          )}
        </div>
      </header>

      {/* Main scrollable content area */}
      <main className="flex-1 w-full px-5 pt-5 pb-24 overflow-y-auto">
        {children}
      </main>

      {/* Bottom tab navigation (mobile) */}
      <nav
        className="absolute bottom-0 left-0 right-0 h-16 bg-white border-t border-neutral-200 z-[900] flex justify-around items-center"
        aria-label="Primary"
      >
        {navItems.map(({ path, label, icon }) => {
          const active = location.pathname === path;
          return (
            <Link
              key={path}
              to={path}
              className={`relative flex flex-col items-center gap-0.5 min-w-[44px] py-1 font-serif text-[11px] font-medium ${
                active ? 'text-dunelm-action' : 'text-neutral-600'
              }`}
            >
              {active && (
                <span
                  aria-hidden
                  className="absolute -top-2 w-6 h-[3px] rounded-b-[3px] bg-dunelm-action"
                />
              )}
              <span className="text-[18px] leading-none" aria-hidden>
                {icon}
              </span>
              <span>{label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
