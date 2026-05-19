import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { LoginScreen } from './LoginScreen';

// Mock navigation
const mockNavigate = jest.fn();
jest.mock('@react-navigation/native-stack', () => ({
  ...jest.requireActual('@react-navigation/native-stack'),
}));

jest.mock('expo-local-authentication', () => ({
  authenticateAsync: jest.fn().mockResolvedValue({ success: false }),
}));

jest.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    login: jest.fn().mockResolvedValue(false),
    socialLogin: jest.fn().mockResolvedValue(false),
    isLoading: false,
  }),
}));

const createProps = () => ({
  navigation: { navigate: mockNavigate } as any,
  route: { key: 'Login', name: 'Login' as const, params: undefined },
});

describe('LoginScreen', () => {
  it('shows error when submitting empty form', async () => {
    const { getByText } = render(<LoginScreen {...createProps()} />);
    fireEvent.press(getByText('Sign In'));
    await waitFor(() => {
      expect(getByText('Email and password are required')).toBeTruthy();
    });
  });

  it('shows error on failed login', async () => {
    const { getByText, getByLabelText } = render(<LoginScreen {...createProps()} />);
    fireEvent.changeText(getByLabelText('Email'), 'test@test.com');
    fireEvent.changeText(getByLabelText('Password'), 'wrongpassword');
    fireEvent.press(getByText('Sign In'));
    await waitFor(() => {
      expect(getByText('Invalid email or password')).toBeTruthy();
    });
  });
});
