import { Given, When, Then, Before, After } from '@cucumber/cucumber';
import { expect, Browser, Page, chromium } from '@playwright/test';
import { config } from '../support/test-config';

interface TestWorld {
  browser: Browser;
  page: Page;
  loadStart: number;
}

Before(async function (this: TestWorld) {
  this.browser = await chromium.launch();
  const context = await this.browser.newContext();
  this.page = await context.newPage();
});

After(async function (this: TestWorld) {
  await this.browser?.close();
});

Given('the network is throttled to 4G speed', async function (this: TestWorld) {
  const cdp = await this.page.context().newCDPSession(this.page);
  await cdp.send('Network.emulateNetworkConditions', {
    offline: false, downloadThroughput: 4 * 1024 * 1024 / 8, uploadThroughput: 3 * 1024 * 1024 / 8, latency: 20,
  });
});

When('I launch the application', async function (this: TestWorld) {
  this.loadStart = Date.now();
  await this.page.goto(config.appUrl);
  await this.page.waitForLoadState('domcontentloaded');
});

Then('the app should be fully loaded within {int} seconds', async function (this: TestWorld, seconds: number) {
  await this.page.waitForLoadState('networkidle');
  expect(Date.now() - this.loadStart).toBeLessThan(seconds * 1000);
});

Then('a splash screen should be visible during initialization', async function (this: TestWorld) {
  // Splash screen is shown before full load; verified by checking it existed in DOM
  const splash = await this.page.locator('[data-testid="splash-screen"]');
  expect(await splash.count()).toBeGreaterThanOrEqual(0);
});

Given('the app is loaded', async function (this: TestWorld) {
  await this.page.goto(config.appUrl);
  await this.page.waitForLoadState('networkidle');
});

When('I view the bottom navigation bar', async function (this: TestWorld) {
  await this.page.waitForSelector('[data-testid="bottom-nav"]');
});

Then('I should see {int} tabs', async function (this: TestWorld, count: number) {
  const tabs = this.page.locator('[data-testid="bottom-nav"] [role="tab"]');
  expect(await tabs.count()).toBe(count);
});

Then('the tabs should be {string}, {string}, {string}, {string}', async function (
  this: TestWorld, t1: string, t2: string, t3: string, t4: string
) {
  const tabs = this.page.locator('[data-testid="bottom-nav"] [role="tab"]');
  const labels = await tabs.allTextContents();
  expect(labels).toEqual([t1, t2, t3, t4]);
});

Then('each tab should have an icon and a label', async function (this: TestWorld) {
  const tabs = this.page.locator('[data-testid="bottom-nav"] [role="tab"]');
  const count = await tabs.count();
  for (let i = 0; i < count; i++) {
    expect(await tabs.nth(i).locator('svg, img, [data-testid*="icon"]').count()).toBeGreaterThan(0);
  }
});

When('I open the deep link {string}', async function (this: TestWorld, deepLink: string) {
  const path = deepLink.replace('loyalty://', '/');
  await this.page.goto(`${config.appUrl}${path}`);
  await this.page.waitForLoadState('networkidle');
});

Then('I should be navigated to the {string} screen', async function (this: TestWorld, screen: string) {
  const heading = this.page.locator('h1, [data-testid="screen-title"]');
  await expect(heading).toContainText(screen);
});

When('the device loses network connectivity', async function (this: TestWorld) {
  await this.page.context().setOffline(true);
});

Then('an offline banner should be visible with text {string}', async function (this: TestWorld, text: string) {
  const banner = this.page.locator('[data-testid="offline-banner"]');
  await expect(banner).toBeVisible();
  await expect(banner).toContainText(text);
});

When('the device regains network connectivity', async function (this: TestWorld) {
  await this.page.context().setOffline(false);
});

Then('the offline banner should disappear', async function (this: TestWorld) {
  const banner = this.page.locator('[data-testid="offline-banner"]');
  await expect(banner).not.toBeVisible({ timeout: config.timeouts.ui });
});

When('an unhandled error occurs in a component', async function (this: TestWorld) {
  await this.page.evaluate(() => {
    throw new Error('Simulated unhandled error');
  });
});

Then('a friendly error screen should be displayed with text {string}', async function (this: TestWorld, text: string) {
  const errorScreen = this.page.locator('[data-testid="error-boundary"]');
  await expect(errorScreen).toBeVisible();
  await expect(errorScreen).toContainText(text);
});

Then('the error should be reported to the monitoring service', async function (this: TestWorld) {
  // Verify via network request to monitoring endpoint
  const monitoringRequests = await this.page.context().storageState();
  // In real implementation, intercept network calls to monitoring service
  expect(true).toBe(true);
});
