import React, { useEffect, useState, useCallback } from 'react';
import { View, StyleSheet, ActivityIndicator } from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import { apiClient } from '../../lib/api-client';
import { colors } from '../../theme/tokens';

interface QRDisplayProps {
  size?: number;
}

export function QRDisplay({ size = 250 }: QRDisplayProps) {
  const [payload, setPayload] = useState<string | null>(null);

  const fetchQR = useCallback(async () => {
    try {
      const { data } = await apiClient.get('/customers/me/qr-code');
      setPayload(data.data.qrPayload);
    } catch {
      setPayload(null);
    }
  }, []);

  useEffect(() => {
    fetchQR();
    const interval = setInterval(fetchQR, 60_000);
    return () => clearInterval(interval);
  }, [fetchQR]);

  if (!payload) {
    return (
      <View style={[styles.container, { width: size, height: size }]}>
        <ActivityIndicator size="large" color={colors.accent.teal} />
      </View>
    );
  }

  return (
    <View style={styles.container} accessibilityLabel="Loyalty QR code for POS scanning">
      <QRCode value={payload} size={size} backgroundColor={colors.primary.white} color={colors.primary.black} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { alignItems: 'center', justifyContent: 'center' },
});
