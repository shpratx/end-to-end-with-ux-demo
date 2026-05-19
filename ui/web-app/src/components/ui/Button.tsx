import { type ButtonHTMLAttributes, forwardRef } from 'react';

type Variant = 'primary' | 'secondary' | 'ghost' | 'promo' | 'danger';
type Size = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  fullWidth?: boolean;
  loading?: boolean;
}

const variantStyles: Record<Variant, string> = {
  primary:
    'bg-primary-black text-primary-white hover:bg-neutral-700 active:bg-neutral-900 disabled:bg-neutral-100 disabled:text-neutral-400',
  secondary:
    'bg-primary-white text-primary-black border border-primary-black hover:bg-neutral-50 active:bg-neutral-100 disabled:bg-neutral-100 disabled:text-neutral-400',
  ghost:
    'bg-transparent text-accent-teal hover:bg-neutral-50 hover:underline disabled:text-neutral-400',
  promo:
    'bg-accent-orange text-primary-white hover:opacity-90 active:opacity-80 disabled:opacity-40',
  danger:
    'bg-error text-primary-white hover:opacity-90 active:opacity-80 disabled:opacity-40',
};

const sizeStyles: Record<Size, string> = {
  sm: 'h-[32px] px-3 text-xs',
  md: 'h-[44px] px-5 text-sm',
  lg: 'h-[52px] px-6 text-md',
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', size = 'md', fullWidth, loading, disabled, children, className = '', ...props }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={[
        'inline-flex items-center justify-center gap-2 rounded-md font-display font-semibold uppercase tracking-normal transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-40',
        variantStyles[variant],
        sizeStyles[size],
        fullWidth ? 'w-full' : '',
        className,
      ].join(' ')}
      {...props}
    >
      {loading ? (
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" role="status" aria-label="Loading" />
      ) : (
        children
      )}
    </button>
  ),
);

Button.displayName = 'Button';
