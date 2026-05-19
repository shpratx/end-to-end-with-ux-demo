import React, { useState } from 'react';
import { View, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { apiClient } from '../../lib/api-client';
import { spacing } from '../../theme/tokens';
import { AuthStackParamList } from '../../navigation/AuthNavigator';

type Props = NativeStackScreenProps<AuthStackParamList, 'ResetPassword'>;

export function ResetPasswordScreen({ navigation }: Props) {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await apiClient.post('/auth/reset-password/request', { email });
      setSent(true);
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <View style={styles.container}>
        <Text variant="displayMedium">Check Your Email</Text>
        <Text variant="bodyLarge" style={styles.subtitle}>If an account exists, a reset link has been sent.</Text>
        <Button title="Back to Sign In" onPress={() => navigation.navigate('Login')} variant="secondary" fullWidth />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text variant="displayMedium" style={styles.title}>Reset Password</Text>
      <Input label="Email" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />
      <Button title="Send Reset Link" onPress={handleSubmit} loading={loading} fullWidth />
      <View style={styles.footer}>
        <Button title="Back to Sign In" onPress={() => navigation.navigate('Login')} variant="ghost" />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: spacing[6], justifyContent: 'center' },
  title: { marginBottom: spacing[8] },
  subtitle: { marginVertical: spacing[6] },
  footer: { marginTop: spacing[4], alignItems: 'center' },
});
