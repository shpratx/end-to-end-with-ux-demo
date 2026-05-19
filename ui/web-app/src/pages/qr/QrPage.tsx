import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../../lib/api-client';
import { useAuthStore } from '../../lib/auth-store';
import { Button } from '../../components/ui/Button';

export function QrPage() {
  const [scanState, setScanState] = useState<'idle' | 'error' | 'success'>('idle');
  const user = useAuthStore((s) => s.user);
  const { data, isLoading } = useQuery({
    queryKey: ['qr-code'],
    queryFn: () => apiClient.get<{ data: { qrPayload: string; expiresAt: string; refreshInSeconds: number } }>('/customers/me/qr-code').then((r) => r.data.data),
    refetchInterval: 60_000,
  });

  // Scan Error Modal
  if (scanState === 'error') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[500px] px-4">
        <div className="bg-white border border-neutral-200 rounded-lg p-6 shadow-lg max-w-sm w-full">
          <p className="font-display text-sm font-bold text-[#CC2200] mb-2">⚠ Scan didn't work</p>
          <p className="text-xs text-neutral-600 mb-3">The till couldn't read your QR code. Try:</p>
          <ul className="text-xs text-neutral-600 leading-relaxed list-disc pl-4 mb-4">
            <li>Increase screen brightness</li>
            <li>Hold phone steady</li>
            <li>Ask staff to enter your phone number instead</li>
          </ul>
          <div className="flex gap-2">
            <Button fullWidth onClick={() => setScanState('idle')}>Try Again</Button>
            <Button variant="secondary" fullWidth onClick={() => alert('Please tell the store associate your phone number for lookup')}>Use Phone #</Button>
          </div>
        </div>
      </div>
    );
  }

  // Success Confirmation
  if (scanState === 'success') {
    return (
      <div className="flex flex-col items-center justify-center min-h-[500px] px-4">
        <div className="bg-green-50 border border-green-200 rounded-lg p-8 text-center max-w-sm w-full">
          <p className="text-2xl mb-2">✓</p>
          <p className="font-display text-base font-bold text-[#1A7A4A]">Points earned!</p>
          <p className="font-display text-2xl font-bold text-black mt-2">+505 points</p>
          <p className="text-sm text-neutral-600 mt-1">New balance: 605 pts</p>
        </div>
        <Button fullWidth className="mt-6 max-w-sm" onClick={() => window.location.href = '/home'}>View Dashboard</Button>
      </div>
    );
  }

  // QR Code Display
  return (
    <div className="flex flex-col items-center gap-4 py-12 px-4">
      <h1 className="font-display text-xl font-bold">Scan at till</h1>
      <p className="text-sm text-neutral-500 text-center">Show this code at any Dunelm till to earn points</p>

      <div className="bg-neutral-50 border border-neutral-200 rounded-sm p-8 flex flex-col items-center gap-3">
        {isLoading ? (
          <div className="w-32 h-32 bg-neutral-200 rounded animate-pulse" />
        ) : (
          <div
            className="w-32 h-32 border-2 border-black rounded"
            style={{ background: 'repeating-linear-gradient(0deg,#e2e8f0 0px,#e2e8f0 4px,#fff 4px,#fff 8px),repeating-linear-gradient(90deg,#e2e8f0 0px,#e2e8f0 4px,#fff 4px,#fff 8px)' }}
            role="img"
            aria-label="QR code for loyalty identification"
          />
        )}
        <p className="font-display text-sm font-bold">{user?.name || 'Member'}</p>
        <p className="text-xs text-[#007A7A]">{user?.tier || 'Member'} tier</p>
      </div>

      <p className="text-xs text-neutral-400">Refreshes every 60 seconds</p>
      <Button variant="secondary" className="max-w-[200px]" onClick={() => alert('Save to wallet functionality coming soon')}>Save to wallet</Button>
    </div>
  );
}
