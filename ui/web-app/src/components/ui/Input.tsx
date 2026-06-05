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
      <div className="flex flex-col gap-1.5">
        <label htmlFor={inputId} className="font-serif text-sm font-semibold text-neutral-700">
          {label}
          {props.required && <span className="text-error ml-1">*</span>}
        </label>
        <input
          ref={ref}
          id={inputId}
          aria-invalid={!!error}
          aria-describedby={error ? errorId : helperText ? helperId : undefined}
          className={[
            'h-[48px] rounded-sm border-[1.5px] px-4 py-3 font-serif text-md text-neutral-700 placeholder:text-neutral-400 transition-colors bg-white',
            'focus:border-dunelm-action focus:outline-none focus:ring-[3px] focus:ring-[#0A8A00]/15',
            'disabled:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed',
            error ? 'border-error' : 'border-neutral-300 hover:border-neutral-400',
            className,
          ].join(' ')}
          {...props}
        />
        {error && (
          <p id={errorId} className="text-xs text-error" role="alert">
            {error}
          </p>
        )}
        {!error && helperText && (
          <p id={helperId} className="text-xs text-neutral-500">
            {helperText}
          </p>
        )}
      </div>
    );
  },
);

Input.displayName = 'Input';
