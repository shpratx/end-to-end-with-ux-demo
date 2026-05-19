import React, { useState, useRef, useEffect } from 'react';
import { View, TextInput, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../hooks/useAuth';
import { colors, spacing, radii } from '../../theme/tokens';
import { AuthStackParamList } from '../../navigation/AuthNavigator';

type Props = NativeStackScreenProps<AuthStackParamList, 'VerifyOtp'>;

export function VerifyOtpScreen({ route }: Props) {
  const { customerId } = route.params;
  const [digits, setDigits] = useState<string[]>(Array(6).fill(''));
  const [countdown, setCountdown] = useState(60);
  const [error, setError] = useState('');
  const refs = useRef<(TextInput | null)[]>([]);
  const { verifyOtp, resendOtp, isLoading } = useAuth();

  useEffect(() => {
    if (countdown <= 0) return;
    const t = setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  const handleChange = (text: string, index: number) => {
    const next = [...digits];
    next[index] = text.slice(-1);
    setDigits(next);
    if (text && index < 5) refs.current[index + 1]?.focus();
    if (next.every((d) => d)) handleSubmit(next.join(''));
  };

  const handleSubmit = async (code: string) => {
    setError('');
    const success = await verifyOtp({ customerId, otpCode: code });
    if (!success) setError('Invalid code. Please try again.');
  };

  const handleResend = async () => {
    await resendOtp(customerId);
    setCountdown(60);
  };

  return (
    <View style={styles.container}>
      <Text variant="displayMedium" style={styles.title}>Verify Email</Text>
      <Text variant="bodyLarge" style={styles.subtitle}>Enter the 6-digit code sent to your email</Text>
      {error ? <Text style={styles.error} accessibilityRole="alert">{error}</Text> : null}
      <View style={styles.row}>
        {digits.map((d, i) => (
          <TextInput
            key={i}
            ref={(r) => { refs.current[i] = r; }}
            style={styles.digit}
            value={d}
            onChangeText={(t) => handleChange(t, i)}
            keyboardType="number-pad"
            maxLength={1}
            accessibilityLabel={`Digit ${i + 1}`}
          />
        ))}
      </View>
      <Button title="Verify" onPress={() => handleSubmit(digits.join(''))} loading={isLoading} fullWidth disabled={digits.some((d) => !d)} />
      <View style={styles.resend}>
        {countdown > 0 ? (
          <Text variant="bodyMedium">Resend in {countdown}s</Text>
        ) : (
          <Button title="Resend Code" onPress={handleResend} variant="ghost" />
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: spacing[6], justifyContent: 'center' },
  title: { marginBottom: spacing[2] },
  subtitle: { marginBottom: spacing[6], color: colors.neutral[600] },
  error: { color: colors.semantic.error, fontSize: 13, marginBottom: spacing[4] },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: spacing[6] },
  digit: {
    width: 48, height: 56, borderWidth: 1, borderColor: colors.neutral[200],
    borderRadius: radii.md, textAlign: 'center', fontSize: 22, fontWeight: '600',
  },
  resend: { marginTop: spacing[4], alignItems: 'center' },
});
