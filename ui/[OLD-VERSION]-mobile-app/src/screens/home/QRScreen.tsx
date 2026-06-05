import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text } from 'react-native-paper';
import { QRDisplay } from '../../components/ui/QRCode';
import { colors, spacing } from '../../theme/tokens';

export function QRScreen() {
  return (
    <View style={styles.container}>
      <Text variant="displayMedium" style={styles.title}>Your QR Code</Text>
      <Text variant="bodyLarge" style={styles.subtitle}>Show this at the till to earn points</Text>
      <QRDisplay size={280} />
      <Text variant="bodyMedium" style={styles.hint}>Refreshes automatically every 60 seconds</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.primary.white, alignItems: 'center', justifyContent: 'center', padding: spacing[6] },
  title: { marginBottom: spacing[2] },
  subtitle: { color: colors.neutral[600], marginBottom: spacing[8] },
  hint: { color: colors.neutral[400], marginTop: spacing[6] },
});
