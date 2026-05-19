import { randomUUID } from 'crypto';
import { config } from './test-config';

interface RequestOptions {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
  auth?: boolean;
  token?: string;
}

interface ApiResponse {
  status: number;
  headers: Headers;
  body: unknown;
  responseTime: number;
}

let cachedToken: string | null = null;

async function getAuthToken(): Promise<string> {
  if (cachedToken) return cachedToken;
  const res = await fetch(`${config.baseUrl}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Idempotency-Key': randomUUID() },
    body: JSON.stringify({ email: config.testUser.email, password: config.testUser.password }),
  });
  const data = (await res.json()) as { accessToken: string };
  cachedToken = data.accessToken;
  return cachedToken;
}

export function resetAuthCache(): void {
  cachedToken = null;
}

export async function apiRequest(path: string, options: RequestOptions = {}): Promise<ApiResponse> {
  const { method = 'GET', body, headers = {}, auth = true, token } = options;
  const correlationId = randomUUID();
  const reqHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Correlation-ID': correlationId,
    ...headers,
  };

  if (auth) {
    reqHeaders['Authorization'] = `Bearer ${token || (await getAuthToken())}`;
  }

  if (method !== 'GET') {
    reqHeaders['Idempotency-Key'] = randomUUID();
  }

  const start = Date.now();
  const res = await fetch(`${config.baseUrl}${path}`, {
    method,
    headers: reqHeaders,
    body: body ? JSON.stringify(body) : undefined,
  });
  const responseTime = Date.now() - start;
  const responseBody = res.headers.get('content-type')?.includes('json')
    ? await res.json()
    : await res.text();

  return { status: res.status, headers: res.headers, body: responseBody, responseTime };
}
