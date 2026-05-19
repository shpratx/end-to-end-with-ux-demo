import { type InputHTMLAttributes, forwardRef } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, id, className = '', ...props }, ref) => {
    const inputId = id ?? label.toLowerCase().replace(/\s+/g, '-');
    const errorId = `${inputId}-error`;
    const helperId = `${inputId}-helper`;

    return (
      <div className="flex flex-col gap-1">
        <label htmlFor={inputId} className="font-display text-sm font-medium text-primary-black">
          {label}
          {props.required && <span className="text-error ml-1">*</span>}
        </label>
        <input
          ref={ref}
          id={inputId}
          aria-invalid={!!error}
          aria-describedby={error ? errorId : helperText ? helperId : undefined}
          className={[
            'h-[48px] rounded-md border px-4 py-3 font-serif text-md text-primary-black placeholder:text-neutral-400 transition-colors',
            'focus:border-accent-teal focus:shadow-xs',
            'disabled:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed',
            error ? 'border-error' : 'border-neutral-200 hover:border-neutral-400',
            className,
          ].join(' ')}
          {...props}
        />
        {error && (
          <p id={errorId} className="text-sm text-error" role="alert">
            {error}
          </p>
        )}
        {!error && helperText && (
          <p id={helperId} className="text-sm text-neutral-500">
            {helperText}
          </p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
