import { MD3LightTheme, configureFonts } from 'react-native-paper';
import { colors, radii } from './tokens';

const fontConfig = {
  displayLarge: { fontFamily: 'System', fontSize: 43, fontWeight: '700' as const, lineHeight: 52 },
  displayMedium: { fontFamily: 'System', fontSize: 31, fontWeight: '600' as const, lineHeight: 37 },
  titleLarge: { fontFamily: 'System', fontSize: 22, fontWeight: '600' as const, lineHeight: 26 },
  titleMedium: { fontFamily: 'System', fontSize: 18, fontWeight: '500' as const, lineHeight: 27 },
  bodyLarge: { fontFamily: 'System', fontSize: 16, fontWeight: '400' as const, lineHeight: 24 },
  bodyMedium: { fontFamily: 'System', fontSize: 13, fontWeight: '400' as const, lineHeight: 20 },
  labelLarge: { fontFamily: 'System', fontSize: 13, fontWeight: '600' as const, lineHeight: 16 },
  labelMedium: { fontFamily: 'System', fontSize: 11, fontWeight: '500' as const, lineHeight: 13 },
};

export const theme = {
  ...MD3LightTheme,
  roundness: radii.md,
  colors: {
    ...MD3LightTheme.colors,
    primary: colors.accent.teal,
    onPrimary: colors.primary.white,
    secondary: colors.primary.black,
    onSecondary: colors.primary.white,
    background: colors.primary.white,
    surface: colors.primary.white,
    error: colors.semantic.error,
    onBackground: colors.primary.black,
    onSurface: colors.primary.black,
    outline: colors.neutral[200],
  },
  fonts: configureFonts({ config: fontConfig }),
};

export type AppTheme = typeof theme;
