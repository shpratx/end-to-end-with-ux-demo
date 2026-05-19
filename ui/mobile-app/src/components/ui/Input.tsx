import React from 'react';
import { View, Text, TextInput, StyleSheet, TextInputProps } from 'react-native';
import { colors, spacing, radii } from '../../theme/tokens';

interface InputProps extends TextInputProps {
  label: string;
  error?: string;
}

export function Input({ label, error, ...props }: InputProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.label} accessibilityRole="text">{label}</Text>
      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholderTextColor={colors.neutral[400]}
        accessibilityLabel={label}
        accessibilityHint={error}
        {...props}
      />
      {error && <Text style={styles.error} accessibilityRole="alert">{error}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { marginBottom: spacing[4] },
  label: { fontSize: 13, fontWeight: '500', color: colors.primary.black, marginBottom: spacing[1] },
  input: {
    height: 48,
    borderWidth: 1,
    borderColor: colors.neutral[200],
    borderRadius: radii.md,
    paddingHorizontal: spacing[4],
    fontSize: 16,
    color: colors.primary.black,
    backgroundColor: colors.primary.white,
  },
  inputError: { borderColor: colors.semantic.error },
  error: { fontSize: 13, color: colors.semantic.error, marginTop: spacing[1] },
});
