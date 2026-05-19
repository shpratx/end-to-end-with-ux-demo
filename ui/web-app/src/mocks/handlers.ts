import { http, HttpResponse } from 'msw';

const BASE = '/api/v1';

// Mock data
const mockUser = {
  id: 'c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6',
  name: 'Clara Test',
  email: 'clara@example.com',
  phone: '+447700900001',
  loyaltyId: '1234567890',
  tier: 'Silver',
  memberSince: '2026-01-15T00:00:00Z',
};

const mockTokens = {
  accessToken: 'mock-access-token-jwt',
  refreshToken: 'mock-refresh-token',
  expiresIn: 900,
};

const mockBalance = { availablePoints: 100, monetaryEquivalent: 1.00, pendingPoints: 0, tierMultiplier: 1.0, lastUpdated: new Date().toISOString() };

const mockTransactions = [
  { id: '1', type: 'bonus', points: 100, balanceAfter: 100, referenceId: 'SIGNUP', channel: 'app', description: 'Welcome bonus', createdAt: '2026-05-19T10:00:00Z' },
];

const mockNotifications = [
  { id: 'n1', title: 'Points earned!', body: 'You earned 150 points on your online purchase.', type: 'transactional', readAt: null, createdAt: '2026-05-18T14:30:00Z' },
  { id: 'n2', title: 'Welcome to Next Loyalty!', body: 'You earned 200 bonus points for signing up.', type: 'transactional', readAt: '2026-01-15T10:01:00Z', createdAt: '2026-01-15T10:00:00Z' },
];

const mockDashboard = {
  balance: mockBalance,
  tier: { tierId: 't1', name: 'Member', threshold: 0, earnRateMultiplier: 1.0, badgeColor: '#999999', benefits: ['Earn points on every purchase'] },
  nextTierProgress: { nextTierName: 'Silver', pointsRequired: 2500, pointsEarned: 100, progressPercent: 4 },
  recentTransactions: mockTransactions.slice(0, 5),
  activePromotions: [{ id: 'p1', name: '2x Points Weekend', description: 'Earn double points on all purchases this weekend!', endsAt: '2026-05-20T23:59:59Z' }],
};

export const handlers = [
  // Auth
  http.post(`${BASE}/auth/register`, () => HttpResponse.json({ data: { customerId: mockUser.id, status: 'pending_verification' } }, { status: 201 })),
  http.post(`${BASE}/auth/verify-otp`, () => HttpResponse.json({ data: { verified: true } })),
  http.post(`${BASE}/auth/resend-otp`, () => HttpResponse.json({ data: { sent: true, nextResendAt: new Date(Date.now() + 60000).toISOString() } })),
  http.post(`${BASE}/auth/login`, () => HttpResponse.json({ data: mockTokens })),
  http.post(`${BASE}/auth/login/social`, () => HttpResponse.json({ data: { ...mockTokens, isNewAccount: false } })),
  http.post(`${BASE}/auth/refresh`, () => HttpResponse.json({ data: { accessToken: 'refreshed-token', expiresIn: 900 } })),
  http.post(`${BASE}/auth/logout`, () => new HttpResponse(null, { status: 204 })),
  http.post(`${BASE}/auth/reset-password/request`, () => HttpResponse.json({ data: { message: 'If an account exists, you will receive a reset link.' } })),
  http.post(`${BASE}/auth/reset-password/confirm`, () => HttpResponse.json({ data: { success: true } })),

  // Customer
  http.get(`${BASE}/customers/me/profile`, () => HttpResponse.json({ data: mockUser })),
  http.get(`${BASE}/customers/me/dashboard`, () => HttpResponse.json({ data: mockDashboard })),
  http.get(`${BASE}/customers/me/qr-code`, () => HttpResponse.json({ data: { qrPayload: btoa(JSON.stringify({ id: mockUser.id, ts: Date.now() })), expiresAt: new Date(Date.now() + 60000).toISOString(), refreshInSeconds: 60 } })),

  // Points
  http.get(`${BASE}/points/balance`, () => HttpResponse.json({ data: mockBalance })),
  http.get(`${BASE}/points/transactions`, () => HttpResponse.json({ data: mockTransactions, meta: { page: 1, pageSize: 20, total: 4 } })),

  // Notifications
  http.get(`${BASE}/notifications`, () => HttpResponse.json({ data: mockNotifications, meta: { page: 1, pageSize: 20, total: 2 } })),
  http.get(`${BASE}/notifications/unread-count`, () => HttpResponse.json({ data: { count: 1 } })),
  http.patch(`${BASE}/notifications/:id/read`, () => HttpResponse.json({ data: { success: true } })),

  // Tiers
  http.get(`${BASE}/tiers`, () => HttpResponse.json({ data: [
    { id: 't1', name: 'Bronze', threshold: 0, multiplier: 1.0 },
    { id: 't2', name: 'Silver', threshold: 1000, multiplier: 1.25 },
    { id: 't3', name: 'Gold', threshold: 5000, multiplier: 1.5 },
    { id: 't4', name: 'Platinum', threshold: 20000, multiplier: 2.0 },
  ] })),

  // Health
  http.get(`${BASE}/health`, () => HttpResponse.json({ status: 'ok', version: '1.0.0' })),
];
