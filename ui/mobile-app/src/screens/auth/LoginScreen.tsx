import React, { useState } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import * as LocalAuthentication from 'expo-local-authentication';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../hooks/useAuth';
import { spacing } from '../../theme/tokens';
import { AuthStackParamList } from '../../navigation/AuthNavigator';

type Props = NativeStackScreenProps<AuthStackParamList, 'Login'>;

export function LoginScreen({ navigation }: Props) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login, socialLogin, isLoading } = useAuth();

  const handleLogin = async () => {
    setError('');
    if (!email || !password) { setError('Email and password are required'); return; }
    const success = await login({ email, password });
    if (!success) setError('Invalid email or password');
  };

  const handleBiometric = async () => {
    const result = await LocalAuthentication.authenticateAsync({ promptMessage: 'Sign in with biometrics' });
    if (result.success) {
      // Biometric success triggers token refresh from secure store
      await login({ email: '', password: '', biometric: true });
    }
  };

  const handleSocial = async (provider: 'google' | 'apple') => {
    await socialLogin(provider);
  };

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <Text variant="displayMedium" style={styles.title}>Sign In</Text>
      {error ? <Text style={styles.error} accessibilityRole="alert">{error}</Text> : null}
      <Input label="Email" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
      <Input label="Password" value={password} onChangeText={setPassword} secureTextEntry />
      <Button title="Sign In" onPress={handleLogin} loading={isLoading} fullWidth />
      <View style={styles.divider}>
        <Button title="Use Biometrics" onPress={handleBiometric} variant="secondary" fullWidth />
      </View>
      <View style={styles.social}>
        <Button title="Continue with Google" onPress={() => handleSocial('google')} variant="secondary" fullWidth />
        <View style={styles.gap} />
        <Button title="Continue with Apple" onPress={() => handleSocial('apple')} variant="secondary" fullWidth />
      </View>
      <View style={styles.footer}>
        <Button title="Forgot password?" onPress={() => navigation.navigate('ResetPassword')} variant="ghost" />
        <Button title="Create account" onPress={() => navigation.navigate('Register')} variant="ghost" />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: spacing[6], justifyContent: 'center' },
  title: { marginBottom: spacing[8] },
  error: { color: '#CC2200', fontSize: 13, marginBottom: spacing[4] },
  divider: { marginTop: spacing[4] },
  social: { marginTop: spacing[4] },
  gap: { height: spacing[2] },
  footer: { marginTop: spacing[6], alignItems: 'center' },
});
