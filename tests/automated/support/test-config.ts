export const config = {
  baseUrl: process.env.API_BASE_URL || 'http://localhost:3000/api/v1',
  appUrl: process.env.APP_URL || 'http://localhost:3000',
  metricsUrl: process.env.METRICS_URL || 'http://localhost:9090',
  logAggregatorUrl: process.env.LOG_AGGREGATOR_URL || 'http://localhost:3100',
  alertManagerUrl: process.env.ALERT_MANAGER_URL || 'http://localhost:9093',
  timeouts: {
    api: Number(process.env.API_TIMEOUT_MS) || 5000,
    ui: Number(process.env.UI_TIMEOUT_MS) || 10000,
    notification: Number(process.env.NOTIFICATION_TIMEOUT_MS) || 30000,
  },
  testUser: {
    email: process.env.TEST_USER_EMAIL || 'test@example.com',
    password: process.env.TEST_USER_PASSWORD || 'TestP@ssword2026!',
    customerId: process.env.TEST_USER_CUSTOMER_ID || 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
  },
  rateLimitThreshold: Number(process.env.RATE_LIMIT_THRESHOLD) || 1000,
};
