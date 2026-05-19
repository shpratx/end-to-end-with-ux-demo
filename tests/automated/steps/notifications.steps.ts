import { Given, When, Then, Before } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { apiRequest, resetAuthCache } from '../support/api-client';
import { config } from '../support/test-config';

interface TestWorld {
  response: { status: number; headers: Headers; body: any; responseTime: number };
  notificationId: string;
  initialUnreadCount: number;
  finalUnreadCount: number;
}

Before(function (this: TestWorld) {
  resetAuthCache();
});

Given('I am authenticated as a test customer', async function () {
  // Auth handled by apiRequest
});

Given('the notification service is running', async function () {
  const res = await apiRequest('/health', { auth: false });
  expect(res.status).toBe(200);
});

When('a {string} event is published for the test customer', async function (this: TestWorld, eventType: string) {
  this.response = await apiRequest('/test/events', {
    method: 'POST',
    body: { type: eventType, customerId: config.testUser.customerId, payload: { points: 76, balance: 2576 } },
  });
});

Then('a push notification should be delivered within {int} seconds', async function (this: TestWorld, seconds: number) {
  const deadline = Date.now() + seconds * 1000;
  let delivered = false;
  while (Date.now() < deadline && !delivered) {
    const res = await apiRequest('/notifications?pageSize=1');
    if (res.status === 200 && (res.body as any).data?.length > 0) {
      delivered = true;
      this.notificationId = (res.body as any).data[0].notificationId;
    }
    if (!delivered) await new Promise((r) => setTimeout(r, 2000));
  }
  expect(delivered).toBe(true);
});

Then('the notification body should contain the substituted points value', async function (this: TestWorld) {
  const res = await apiRequest('/notifications?pageSize=1');
  expect((res.body as any).data[0].body).toContain('76');
});

Given('the push delivery endpoint is configured to fail', async function () {
  await apiRequest('/test/config/push-failure', { method: 'POST', body: { enabled: true } });
});

Then('the service should retry delivery {int} times', async function (_retries: number) {
  await new Promise((r) => setTimeout(r, 36000));
  const res = await apiRequest(`/test/delivery-attempts/${config.testUser.customerId}`);
  expect((res.body as any).attempts).toBe(_retries);
});

Then('the retry intervals should follow exponential backoff of 1s, 5s, 30s', async function () {
  const res = await apiRequest(`/test/delivery-attempts/${config.testUser.customerId}`);
  const intervals = (res.body as any).intervals as number[];
  expect(intervals[0]).toBeCloseTo(1000, -2);
  expect(intervals[1]).toBeCloseTo(5000, -2);
  expect(intervals[2]).toBeCloseTo(30000, -2);
});

Then('the notification should be marked as failed after all retries', async function () {
  const res = await apiRequest(`/test/delivery-status/${config.testUser.customerId}`);
  expect((res.body as any).status).toBe('failed');
});

When('I wait for notification processing to complete', async function () {
  await new Promise((r) => setTimeout(r, 5000));
});

When('I send a GET request to {string}', async function (this: TestWorld, path: string) {
  this.response = await apiRequest(path);
});

Then('the response status should be {int}', function (this: TestWorld, status: number) {
  expect(this.response.status).toBe(status);
});

Then('the response should contain the notification with correct title and body', function (this: TestWorld) {
  const data = (this.response.body as any).data;
  expect(data.length).toBeGreaterThan(0);
  expect(data[0]).toHaveProperty('title');
  expect(data[0]).toHaveProperty('body');
});

Then('notifications should be ordered newest first', function (this: TestWorld) {
  const data = (this.response.body as any).data;
  if (data.length > 1) {
    expect(new Date(data[0].createdAt).getTime()).toBeGreaterThanOrEqual(new Date(data[1].createdAt).getTime());
  }
});

Given('a notification exists for the test customer', async function (this: TestWorld) {
  await apiRequest('/test/events', {
    method: 'POST',
    body: { type: 'points.earned', customerId: config.testUser.customerId, payload: { points: 10, balance: 100 } },
  });
  await new Promise((r) => setTimeout(r, 5000));
  const res = await apiRequest('/notifications?pageSize=1');
  this.notificationId = (res.body as any).data[0].notificationId;
});

When('I get the unread notification count', async function (this: TestWorld) {
  const res = await apiRequest('/notifications/unread-count');
  this.initialUnreadCount = (res.body as any).unreadCount;
});

When('I mark the notification as read', async function (this: TestWorld) {
  await apiRequest(`/notifications/${this.notificationId}/read`, { method: 'PATCH' });
});

When('I get the unread notification count again', async function (this: TestWorld) {
  const res = await apiRequest('/notifications/unread-count');
  this.finalUnreadCount = (res.body as any).unreadCount;
});

Then('the unread count should have decremented by {int}', function (this: TestWorld, decrement: number) {
  expect(this.finalUnreadCount).toBe(this.initialUnreadCount - decrement);
});
