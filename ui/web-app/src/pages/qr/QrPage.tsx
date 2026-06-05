import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../../lib/api-client';
import { useAuthStore } from '../../lib/auth-store';
import { Button } from '../../components/ui/Button';

/**
 * Visual-only QR code. The pattern is decorative — it doesn't encode any data
 * — but it has the three finder squares and a plausible-looking module grid so
 * it reads as a real QR code in the wireframes.
 */
function SampleQrCode() {
  const SIZE = 21; // standard QR v1 module count
  // Deterministic pseudo-random fill (no Date.now / Math.random — stable across renders)
  const filled = (r: number, c: number) => ((r * 7 + c * 13 + r * c) % 5) < 2;
  const isFinder = (r: number, c: number) => {
    const inBox = (br: number, bc: number) =>
      r >= br && r < br + 7 && c >= bc && c < bc + 7;
    return inBox(0, 0) || inBox(0, SIZE - 7) || inBox(SIZE - 7, 0);
  };
  const finderModule = (r: number, c: number) => {
    const inRing = (br: number, bc: number) => {
      const lr = r - br, lc = c - bc;
      if (lr < 0 || lr > 6 || lc < 0 || lc > 6) return null;
      // outer ring (filled), inner ring (empty), centre 3x3 (filled)
      if (lr === 0 || lr === 6 || lc === 0 || lc === 6) return true;
      if (lr === 1 || lr === 5 || lc === 1 || lc === 5) return false;
      return true;
    };
    return inRing(0, 0) ?? inRing(0, SIZE - 7) ?? inRing(SIZE - 7, 0) ?? false;
  };

  const cells = [];
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      const on = isFinder(r, c) ? finderModule(r, c) : filled(r, c);
      if (on) cells.push(<rect key={`${r}-${c}`} x={c} y={r} width={1} height={1} />);
    }
  }

  return (
    <svg
      viewBox={`0 0 ${SIZE} ${SIZE}`}
      className="w-32 h-32 rounded border border-neutral-300 bg-white p-1"
      shapeRendering="crispEdges"
      role="img"
      aria-label="Sample QR code for loyalty identification"
    >
      <rect x={0} y={0} width={SIZE} height={SIZE} fill="#FFFFFF" />
      <g fill="#1C1C1A">{cells}</g>
    </svg>
  );
}

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
          <p className="font-display text-base font-bold text-[#0A7A0A]">Points earned!</p>
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
          <SampleQrCode />
        )}
        <p className="font-display text-sm font-bold">{user?.name || 'Member'}</p>
        <p className="text-xs text-[#0A8A00]">{user?.tier || 'Member'} tier</p>
      </div>

      <p className="text-xs text-neutral-400">Refreshes every 60 seconds</p>
      <Button variant="secondary" className="max-w-[200px]" onClick={() => alert('Save to wallet functionality coming soon')}>Save to wallet</Button>
    </div>
  );
}
