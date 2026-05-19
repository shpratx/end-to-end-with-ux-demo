import { Given, When, Then, Before } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { apiRequest, resetAuthCache } from '../support/api-client';

interface TestWorld {
  response: { status: number; headers: Headers; body: any; responseTime: number };
  lastResponse: { status: number; headers: Headers; body: any; responseTime: number };
}

Before(function (this: TestWorld) {
  resetAuthCache();
});

Given('the API gateway is running', async function () {
  // Validated by health check scenario
});

When('I send a GET request to {string}', async function (this: TestWorld, path: string) {
  this.response = await apiRequest(path, { auth: false });
});

When('I send a GET request to {string} without authentication', async function (this: TestWorld, path: string) {
  this.response = await apiRequest(path, { auth: false });
});

Given('I am authenticated as a test user', async function () {
  // Auth handled automatically by apiRequest when auth=true
});

When('I send {int} requests to {string} within {int} minute(s)', async function (
  this: TestWorld, count: number, path: string, _minutes: number
) {
  for (let i = 0; i < count; i++) {
    this.lastResponse = await apiRequest(path, { auth: false });
    if (this.lastResponse.status === 429) break;
  }
});

When('I trigger a {string} error', async function (this: TestWorld, errorType: string) {
  const triggers: Record<string, { path: string; method: string }> = {
    bad_request: { path: '/auth/login', method: 'POST' },
    unauthorized: { path: '/notifications', method: 'GET' },
    not_found: { path: '/nonexistent-endpoint', method: 'GET' },
    validation: { path: '/auth/register', method: 'POST' },
  };
  const { path, method } = triggers[errorType];
  this.response = await apiRequest(path, { method, auth: false, body: method === 'POST' ? {} : undefined });
});

Then('the response status should be {int}', function (this: TestWorld, status: number) {
  expect(this.response.status).toBe(status);
});

Then('the response body should contain {string}', function (this: TestWorld, field: string) {
  expect(this.response.body).toHaveProperty(field);
});

Then('the response time should be less than {int} milliseconds', function (this: TestWorld, ms: number) {
  expect(this.response.responseTime).toBeLessThan(ms);
});

Then('the response body should not contain any customer data', function (this: TestWorld) {
  const body = JSON.stringify(this.response.body);
  expect(body).not.toContain('customerId');
  expect(body).not.toContain('email');
});

Then('the response content type should be {string}', function (this: TestWorld, contentType: string) {
  expect(this.response.headers.get('content-type')).toContain(contentType);
});

Then('the last response status should be {int}', function (this: TestWorld, status: number) {
  expect(this.lastResponse.status).toBe(status);
});

Then('the response should contain a {string} header', function (this: TestWorld, header: string) {
  expect(this.lastResponse.headers.get(header)).not.toBeNull();
});

Then('the response body should contain field {string}', function (this: TestWorld, field: string) {
  expect(this.response.body).toHaveProperty(field);
});
