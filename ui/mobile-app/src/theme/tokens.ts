export const colors = {
  primary: { black: '#000000', white: '#FFFFFF' },
  neutral: {
    0: '#FFFFFF', 50: '#F9F9F9', 100: '#F2F2F2', 200: '#E5E5E5',
    300: '#CCCCCC', 400: '#999999', 500: '#777777', 600: '#555555',
    700: '#333333', 900: '#000000',
  },
  accent: { teal: '#007A7A', orange: '#FF6A3B' },
  semantic: { success: '#1A7A4A', warning: '#C47A00', error: '#CC2200', info: '#007A7A' },
} as const;

export const spacing = {
  0: 0, 1: 4, 2: 8, 3: 12, 4: 16, 5: 20, 6: 24, 8: 32, 10: 40, 12: 48, 16: 64, 20: 80,
} as const;

export const typography = {
  sizes: { xs: 11, sm: 13, md: 16, lg: 18, xl: 22, '2xl': 31, '3xl': 43 },
  lineHeights: { tight: 1.2, normal: 1.5, relaxed: 1.75 },
  weights: { regular: '400', medium: '500', semiBold: '600', bold: '700' },
} as const;

export const radii = {
  none: 0, sm: 2, md: 4, lg: 8, xl: 12, full: 9999,
} as const;

export const shadows = {
  xs: { shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 2, elevation: 1 },
  sm: { shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 4, elevation: 2 },
  md: { shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.1, shadowRadius: 12, elevation: 3 },
  lg: { shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.14, shadowRadius: 24, elevation: 4 },
} as const;
