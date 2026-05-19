import { RegisterPage } from './pages/auth/RegisterPage';
import { LoginPage } from './pages/auth/LoginPage';
import { VerifyOtpPage } from './pages/auth/VerifyOtpPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { DashboardPage } from './pages/home/DashboardPage';
import { HistoryPage } from './pages/history/HistoryPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { NotificationsPage } from './pages/notifications/NotificationsPage';
import { QrPage } from './pages/qr/QrPage';
import { BasketPage } from './pages/checkout/BasketPage';
import { PaymentPage } from './pages/checkout/PaymentPage';
import { ConfirmationPage } from './pages/checkout/ConfirmationPage';
import { ClaimPage } from './pages/claims/ClaimPage';

export const routes = [
  // Auth (public)
  { path: '/auth/register', element: <RegisterPage />, requiresAuth: false },
  { path: '/auth/login', element: <LoginPage />, requiresAuth: false },
  { path: '/auth/verify', element: <VerifyOtpPage />, requiresAuth: false },
  { path: '/auth/reset-password', element: <ResetPasswordPage />, requiresAuth: false },
  // Core (authenticated)
  { path: '/home', element: <DashboardPage />, requiresAuth: true },
  { path: '/history', element: <HistoryPage />, requiresAuth: true },
  { path: '/profile', element: <ProfilePage />, requiresAuth: true },
  { path: '/notifications', element: <NotificationsPage />, requiresAuth: true },
  { path: '/qr', element: <QrPage />, requiresAuth: true },
  // Checkout
  { path: '/checkout/basket', element: <BasketPage />, requiresAuth: false },
  { path: '/checkout/payment', element: <PaymentPage />, requiresAuth: false },
  { path: '/checkout/confirmation', element: <ConfirmationPage />, requiresAuth: false },
  // Claims (authenticated)
  { path: '/loyalty/claim', element: <ClaimPage />, requiresAuth: true },
];
