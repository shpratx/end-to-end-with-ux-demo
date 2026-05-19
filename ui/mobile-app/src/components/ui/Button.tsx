import React from 'react';
import { Pressable, StyleSheet, Text, ActivityIndicator, ViewStyle, TextStyle } from 'react-native';
import { colors, spacing, radii } from '../../theme/tokens';

type Variant = 'primary' | 'secondary' | 'ghost';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: Variant;
  disabled?: boolean;
  loading?: boolean;
  fullWidth?: boolean;
  accessibilityLabel?: string;
}

const variantStyles: Record<Variant, { container: ViewStyle; text: TextStyle }> = {
  primary: {
    container: { backgroundColor: colors.primary.black },
    text: { color: colors.primary.white },
  },
  secondary: {
    container: { backgroundColor: colors.primary.white, borderWidth: 1, borderColor: colors.primary.black },
    text: { color: colors.primary.black },
  },
  ghost: {
    container: { backgroundColor: 'transparent' },
    text: { color: colors.accent.teal },
  },
};

export function Button({ title, onPress, variant = 'primary', disabled, loading, fullWidth, accessibilityLabel }: ButtonProps) {
  const vs = variantStyles[variant];
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled || loading}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel ?? title}
      accessibilityState={{ disabled: disabled || loading }}
      style={[styles.base, vs.container, fullWidth && styles.fullWidth, disabled && styles.disabled]}
    >
      {loading ? (
        <ActivityIndicator size="small" color={vs.text.color as string} />
      ) : (
        <Text style={[styles.text, vs.text]}>{title}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 44,
    paddingVertical: spacing[2],
    paddingHorizontal: spacing[5],
    borderRadius: radii.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  fullWidth: { width: '100%' },
  disabled: { opacity: 0.4 },
  text: { fontSize: 13, fontWeight: '600', textTransform: 'uppercase', letterSpacing: 0 },
});
