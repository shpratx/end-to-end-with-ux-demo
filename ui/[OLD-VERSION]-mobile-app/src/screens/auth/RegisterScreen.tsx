import React, { useState } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../hooks/useAuth';
import { spacing } from '../../theme/tokens';
import { AuthStackParamList } from '../../navigation/AuthNavigator';

type Props = NativeStackScreenProps<AuthStackParamList, 'Register'>;

export function RegisterScreen({ navigation }: Props) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const { register, isLoading } = useAuth();

  const validate = (): boolean => {
    const e: Record<string, string> = {};
    if (!name.trim()) e.name = 'Name is required';
    if (!email.includes('@')) e.email = 'Email must include an @ symbol';
    if (!/^\+[1-9]\d{1,14}$/.test(phone)) e.phone = 'Phone must be in international format';
    if (password.length < 12) e.password = 'Password must be at least 12 characters';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    const result = await register({ name, email, phone, password, termsAndConditionsVersion: '2.1' });
    if (result?.customerId) {
      navigation.navigate('VerifyOtp', { customerId: result.customerId });
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <Text variant="displayMedium" style={styles.title}>Create Account</Text>
      <Input label="Full Name" value={name} onChangeText={setName} error={errors.name} autoCapitalize="words" />
      <Input label="Email" value={email} onChangeText={setEmail} error={errors.email} keyboardType="email-address" autoCapitalize="none" />
      <Input label="Phone" value={phone} onChangeText={setPhone} error={errors.phone} keyboardType="phone-pad" placeholder="+44" />
      <Input label="Password" value={password} onChangeText={setPassword} error={errors.password} secureTextEntry />
      <Button title="Create Account" onPress={handleSubmit} loading={isLoading} fullWidth />
      <View style={styles.footer}>
        <Button title="Already have an account? Sign in" onPress={() => navigation.navigate('Login')} variant="ghost" />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, padding: spacing[6], justifyContent: 'center' },
  title: { marginBottom: spacing[8] },
  footer: { marginTop: spacing[4], alignItems: 'center' },
});
