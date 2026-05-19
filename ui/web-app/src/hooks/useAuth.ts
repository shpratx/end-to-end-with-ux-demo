import { useMutation } from '@tanstack/react-query';
import { apiClient } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { type AxiosError } from 'axios';

interface RegisterPayload {
  name: string;
  email: string;
  phone: string;
  password: string;
  termsAndConditionsVersion: string;
}

interface RegisterResponse {
  customerId: string;
  status: string;
  message: string;
}

interface LoginPayload {
  email: string;
  password: string;
}

interface AuthTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

interface VerifyPayload {
  customerId: string;
  otpCode: string;
}

interface VerifyResponse {
  verified: boolean;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface ApiError {
  detail?: string;
  title?: string;
}

function extractErrorMessage(err: unknown): string {
  const axiosErr = err as AxiosError<ApiError>;
  return axiosErr.response?.data?.detail ?? axiosErr.message ?? 'An error occurred';
}

export function useRegister() {
  return useMutation<RegisterResponse, Error, RegisterPayload>({
    mutationFn: async (payload) => {
      const res = await apiClient.post<RegisterResponse>('/auth/register', payload, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      });
      return res.data;
    },
    onError: (err) => {
      err.message = extractErrorMessage(err);
    },
  });
}

export function useLogin() {
  const login = useAuthStore((s) => s.login);

  return useMutation<AuthTokenResponse, Error, LoginPayload>({
    mutationFn: async (payload) => {
      const res = await apiClient.post('/auth/login', payload, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      });
      return res.data.data ?? res.data;
    },
    onSuccess: (data) => {
      login(data.accessToken, data.refreshToken, { customerId: '', name: '', email: '', tier: '' });
    },
    onError: (err) => {
      err.message = extractErrorMessage(err);
    },
  });
}

export function useVerifyOtp() {
  const login = useAuthStore((s) => s.login);

  return useMutation<VerifyResponse, Error, VerifyPayload>({
    mutationFn: async (payload) => {
      const res = await apiClient.post<VerifyResponse>('/auth/verify-otp', payload, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      });
      return res.data;
    },
    onSuccess: (data) => {
      login(data.accessToken, data.refreshToken, { customerId: '', name: '', email: '', tier: '' });
    },
    onError: (err) => {
      err.message = extractErrorMessage(err);
    },
  });
}

export function useLogout() {
  const { refreshToken, logout } = useAuthStore.getState();

  return useMutation<void, Error, void>({
    mutationFn: async () => {
      await apiClient.post('/auth/logout', { refreshToken }, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      });
    },
    onSettled: () => {
      logout();
    },
  });
}
