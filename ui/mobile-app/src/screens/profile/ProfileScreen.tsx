import React, { useState } from 'react';
import { View, ScrollView, StyleSheet, Switch } from 'react-native';
import { Text } from 'react-native-paper';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { apiClient } from '../../lib/api-client';
import { useAuthStore } from '../../lib/auth-store';
import { clearTokens } from '../../lib/api-client';
import { colors, spacing } from '../../theme/tokens';

interface Profile {
  name: string;
  email: string;
  phone: string;
  tier: string;
  loyaltyId: string;
  memberSince: string;
}

interface Preferences {
  transactional: boolean;
  promotional: boolean;
  system: boolean;
  emailCopies: boolean;
}

export function ProfileScreen() {
  const queryClient = useQueryClient();
  const logout = useAuthStore((s) => s.logout);
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState('');

  const { data: profile } = useQuery<Profile>({
    queryKey: ['profile'],
    queryFn: async () => (await apiClient.get('/customers/me/profile')).data,
  });

  const { data: prefs } = useQuery<Preferences>({
    queryKey: ['preferences'],
    queryFn: async () => (await apiClient.get('/customers/me/preferences')).data,
  });

  const updateProfile = useMutation({
    mutationFn: (body: { name: string }) => apiClient.put('/customers/me/profile', body),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['profile'] }); setEditing(false); },
  });

  const updatePrefs = useMutation({
    mutationFn: (body: Partial<Preferences>) => apiClient.put('/customers/me/preferences', body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['preferences'] }),
  });

  const handleLogout = async () => {
    await clearTokens();
    logout();
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text variant="displayMedium" style={styles.title}>Profile</Text>

      {profile && (
        <View style={styles.section}>
          <Text variant="bodyMedium" style={styles.label}>Member since {profile.memberSince}</Text>
          <Text variant="bodyMedium" style={styles.label}>Loyalty ID: {profile.loyaltyId}</Text>
          {editing ? (
            <>
              <Input label="Name" value={name || profile.name} onChangeText={setName} />
              <Button title="Save" onPress={() => updateProfile.mutate({ name: name || profile.name })} loading={updateProfile.isPending} fullWidth />
            </>
          ) : (
            <>
              <Text variant="titleLarge" style={styles.name}>{profile.name}</Text>
              <Text variant="bodyLarge">{profile.email}</Text>
              <Text variant="bodyLarge">{profile.phone}</Text>
              <Button title="Edit Profile" onPress={() => setEditing(true)} variant="secondary" />
            </>
          )}
        </View>
      )}

      {prefs && (
        <View style={styles.section}>
          <Text variant="titleLarge" style={styles.sectionTitle}>Notification Preferences</Text>
          {(['promotional', 'system', 'emailCopies'] as const).map((key) => (
            <View key={key} style={styles.prefRow}>
              <Text variant="bodyLarge" style={styles.prefLabel}>{key}</Text>
              <Switch
                value={prefs[key]}
                onValueChange={(v) => updatePrefs.mutate({ [key]: v })}
                trackColor={{ true: colors.accent.teal }}
              />
            </View>
          ))}
        </View>
      )}

      <Button title="Sign Out" onPress={handleLogout} variant="ghost" fullWidth />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.primary.white },
  content: { padding: spacing[4], paddingTop: spacing[12] },
  title: { marginBottom: spacing[6] },
  section: { marginBottom: spacing[6] },
  sectionTitle: { marginBottom: spacing[3] },
  label: { color: colors.neutral[500], marginBottom: spacing[1] },
  name: { marginVertical: spacing[2] },
  prefRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: spacing[2] },
  prefLabel: { textTransform: 'capitalize' },
});
