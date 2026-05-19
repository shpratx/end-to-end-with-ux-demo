import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';
import * as SecureStore from 'expo-secure-store';

interface AuthState {
  isAuthenticated: boolean;
  customerId: string | null;
  setAuthenticated: (customerId: string) => void;
  logout: () => void;
}

const secureStorage = {
  getItem: async (name: string) => SecureStore.getItemAsync(name),
  setItem: async (name: string, value: string) => SecureStore.setItemAsync(name, value),
  removeItem: async (name: string) => SecureStore.deleteItemAsync(name),
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      customerId: null,
      setAuthenticated: (customerId) => set({ isAuthenticated: true, customerId }),
      logout: () => set({ isAuthenticated: false, customerId: null }),
    }),
    { name: 'auth-state', storage: createJSONStorage(() => secureStorage) },
  ),
);
