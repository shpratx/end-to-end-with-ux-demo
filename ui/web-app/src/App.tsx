import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthGuard } from './components/layout/AuthGuard';
import { AppShell } from './components/layout/AppShell';
import { useAuthStore } from './lib/auth-store';
import { routes } from './routes';

export function App() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  return (
    <Routes>
      {routes.map(({ path, element, requiresAuth }) =>
        requiresAuth ? (
          <Route
            key={path}
            path={path}
            element={
              <AuthGuard>
                <AppShell>{element}</AppShell>
              </AuthGuard>
            }
          />
        ) : (
          <Route key={path} path={path} element={element} />
        ),
      )}
      <Route path="*" element={<Navigate to={isAuthenticated ? '/home' : '/auth/register'} replace />} />
    </Routes>
  );
}
