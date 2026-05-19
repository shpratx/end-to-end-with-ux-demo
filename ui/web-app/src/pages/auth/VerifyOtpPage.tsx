import { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { useVerifyOtp } from '../../hooks/useAuth';

export function VerifyOtpPage() {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [confirmed, setConfirmed] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const navigate = useNavigate();
  const location = useLocation();
  const customerId = (location.state as { customerId?: string })?.customerId ?? '';
  const { mutate: verify, isPending, error } = useVerifyOtp();

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const code = otp.join('');
    if (code.length !== 6) return;
    verify({ customerId, otpCode: code }, { onSuccess: () => setConfirmed(true) });
  };

  // Screen 5.0: Confirmation & QR Code
  if (confirmed) {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4 text-center">
        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <span className="text-2xl">✓</span>
        </div>
        <h1 className="font-display text-2xl font-bold mb-2">You're a Next loyalty member</h1>
        <p className="text-sm text-neutral-600 mb-6">Welcome bonus: <strong>100 points</strong> added to your account</p>

        <div className="bg-neutral-50 border border-neutral-200 rounded-sm p-6 mb-6">
          <p className="font-display text-sm font-semibold text-primary-black mb-1">Clara M. · Member</p>
          <p className="text-xs text-neutral-500 mb-4">Scan at any Next till to earn points</p>
          <div className="w-32 h-32 mx-auto bg-[repeating-linear-gradient(0deg,#e2e8f0_0px,#e2e8f0_3px,#fff_3px,#fff_6px),repeating-linear-gradient(90deg,#e2e8f0_0px,#e2e8f0_3px,#fff_3px,#fff_6px)] border-2 border-primary-black rounded" aria-label="QR code for loyalty identification" />
        </div>

        <div className="bg-neutral-50 rounded-sm p-4 mb-6">
          <p className="text-xs text-neutral-500 uppercase tracking-wide mb-1">Your starting balance</p>
          <p className="font-display text-lg font-bold text-primary-black">100 pts · worth £1.00</p>
        </div>

        <Button fullWidth onClick={() => navigate('/home')}>Go to my loyalty dashboard</Button>
        <Button variant="secondary" fullWidth className="mt-3" onClick={() => alert('Save to wallet functionality coming soon')}>Save QR code to wallet</Button>
        <p className="text-xs text-neutral-400 mt-4 text-center">Download the Next app for the full experience</p>
      </div>
    );
  }

  // OTP Input Screen
  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <h1 className="font-display text-2xl font-bold mb-2">Verify your email</h1>
      <p className="text-sm text-neutral-600 mb-6">We've sent a 6-digit code to your email address.</p>

      {error && (
        <div className="mb-6 rounded-md bg-red-50 border border-red-200 p-4 text-sm text-red-700" role="alert">{error.message}</div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col items-center gap-6">
        <div className="flex gap-3" role="group" aria-label="One-time password">
          {otp.map((digit, i) => (
            <input
              key={i}
              ref={(el) => { inputRefs.current[i] = el; }}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleChange(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              className="w-12 h-14 text-center text-xl font-bold border border-neutral-200 rounded focus:border-[#007A7A] focus:outline-none focus:ring-2 focus:ring-[#007A7A]/20"
              aria-label={`Digit ${i + 1}`}
            />
          ))}
        </div>

        <Button type="submit" fullWidth loading={isPending} disabled={otp.join('').length !== 6}>
          Verify
        </Button>
      </form>

      <div className="text-center mt-6">
        {countdown > 0 ? (
          <p className="text-sm text-neutral-400">Resend code in {countdown}s</p>
        ) : (
          <button className="font-display text-sm text-[#007A7A] underline" onClick={() => setCountdown(60)}>Resend code</button>
        )}
      </div>
    </div>
  );
}
