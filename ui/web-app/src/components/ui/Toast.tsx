import { useEffect, useState } from 'react';

type ToastVariant = 'success' | 'warning' | 'error' | 'info';

interface ToastProps {
  message: string;
  variant?: ToastVariant;
  onDismiss: () => void;
  autoDismissMs?: number;
}

const variantStyles: Record<ToastVariant, string> = {
  success: 'bg-success',
  warning: 'bg-warning',
  error: 'bg-error',
  info: 'bg-neutral-900',
};

export function Toast({ message, variant = 'info', onDismiss, autoDismissMs }: ToastProps) {
  const [visible, setVisible] = useState(true);
  const duration = autoDismissMs ?? (variant === 'success' || variant === 'info' ? 4000 : undefined);

  useEffect(() => {
    if (!duration) return;
    const timer = setTimeout(() => {
      setVisible(false);
      onDismiss();
    }, duration);
    return () => clearTimeout(timer);
  }, [duration, onDismiss]);

  if (!visible) return null;

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`fixed bottom-6 right-6 z-[9000] max-w-[400px] rounded-lg p-4 shadow-lg text-primary-white text-sm flex items-center gap-3 animate-[slideUp_200ms_ease] ${variantStyles[variant]}`}
    >
      <span className="flex-1">{message}</span>
      <button
        onClick={onDismiss}
        className="opacity-70 hover:opacity-100 text-primary-white"
        aria-label="Dismiss"
      >
        ✕
      </button>
    </div>
  );
}
