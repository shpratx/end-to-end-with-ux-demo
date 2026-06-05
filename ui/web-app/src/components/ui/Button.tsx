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
    'bg-dunelm-action text-white hover:bg-dunelm-action-hover active:bg-[#045200] disabled:bg-[#BFD9BC] disabled:text-white',
  secondary:
    'bg-white text-neutral-700 border-[1.5px] border-neutral-300 hover:border-neutral-700 active:bg-neutral-50 disabled:opacity-50',
  ghost:
    'bg-transparent text-dunelm-link hover:underline disabled:text-neutral-400',
  promo:
    'bg-dunelm-sale text-white hover:bg-dunelm-sale-dark active:opacity-90 disabled:opacity-40',
  danger:
    'bg-error text-white hover:opacity-90 active:opacity-80 disabled:opacity-40',
};

const sizeStyles: Record<Size, string> = {
  sm: 'h-[36px] px-4 text-xs',
  md: 'h-[44px] px-5 text-sm',
  lg: 'h-[52px] px-7 text-md',
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'primary', size = 'md', fullWidth, loading, disabled, children, className = '', ...props }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={[
        'inline-flex items-center justify-center gap-2 rounded-full font-serif font-semibold tracking-normal transition-colors cursor-pointer disabled:cursor-not-allowed',
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
