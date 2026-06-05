import { useState } from 'react';
import { apiClient, setTokens } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';

interface RegisterPayload {
  name: string;
  email: string;
  phone: string;
  password: string;
  termsAndConditionsVersion: string;
}

interface LoginPayload {
  email: string;
  password: string;
  biometric?: boolean;
}

export function useAuth() {
  const [isLoading, setIsLoading] = useState(false);
  const setAuthenticated = useAuthStore((s) => s.setAuthenticated);

  const register = async (payload: RegisterPayload) => {
    setIsLoading(true);
    try {
      const { data } = await apiClient.post('/auth/register', payload);
      return data as { customerId: string };
    } catch {
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (payload: LoginPayload) => {
    setIsLoading(true);
    try {
      const { data } = await apiClient.post('/auth/login', payload);
      await setTokens(data.accessToken, data.refreshToken);
      setAuthenticated(data.customerId ?? 'user');
      return true;
    } catch {
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const socialLogin = async (provider: 'google' | 'apple') => {
    setIsLoading(true);
    try {
      const { data } = await apiClient.post('/auth/login/social', { provider, idToken: '' });
      await setTokens(data.accessToken, data.refreshToken);
      setAuthenticated('user');
      return true;
    } catch {
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const verifyOtp = async (payload: { customerId: string; otpCode: string }) => {
    setIsLoading(true);
    try {
      const { data } = await apiClient.post('/auth/verify-otp', payload);
      if (data.accessToken) {
        await setTokens(data.accessToken, data.refreshToken);
        setAuthenticated(payload.customerId);
      }
      return data.verified as boolean;
    } catch {
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const resendOtp = async (customerId: string) => {
    await apiClient.post('/auth/resend-otp', { customerId });
  };

  return { register, login, socialLogin, verifyOtp, resendOtp, isLoading };
}

// Simple offline detection hook
export function useNetInfo() {
  // In production, use @react-native-community/netinfo
  return { isConnected: true };
}
