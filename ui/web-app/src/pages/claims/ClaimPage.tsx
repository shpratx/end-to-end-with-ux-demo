import { useState } from 'react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';

export function ClaimPage() {
  const [status, setStatus] = useState<'form' | 'submitted' | 'rejected'>('form');

  if (status === 'submitted') {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4 text-center">
        <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"><span className="text-xl">✓</span></div>
        <h1 className="font-display text-xl font-bold mb-2">Claim submitted</h1>
        <p className="text-sm text-neutral-600 leading-relaxed">We'll verify your receipt and add points within 48 hours. You'll receive a notification when it's processed.</p>
        <Button fullWidth className="mt-6" onClick={() => window.location.href = '/home'}>Back to dashboard</Button>
      </div>
    );
  }

  if (status === 'rejected') {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4">
        <div className="bg-red-50 border border-red-200 rounded-sm p-4">
          <p className="font-display text-sm font-bold text-[#CC2200] mb-1">✗ Claim not approved</p>
          <p className="text-xs text-red-800 leading-relaxed">We couldn't verify receipt #12345. This may be because the receipt is older than 7 days or the details don't match our records.</p>
        </div>
        <div className="flex gap-2 mt-4">
          <Button fullWidth onClick={() => setStatus('form')}>Try Again</Button>
          <Button variant="secondary" fullWidth onClick={() => window.location.href = 'mailto:loyalty@Dunelm.co.uk'}>Contact Us</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <h1 className="font-display text-xl font-bold mb-2">Claim missed points</h1>
      <p className="text-sm text-neutral-600 mb-6 leading-relaxed">Forgot to scan? Enter your receipt details and we'll add the points to your account.</p>

      <form className="flex flex-col gap-4" onSubmit={(e) => { e.preventDefault(); setStatus('submitted'); }}>
        <Input label="Receipt number" required />
        <div>
          <label className="block text-xs font-semibold text-black mb-1">Store</label>
          <select className="w-full bg-neutral-50 border border-neutral-200 rounded py-2.5 px-3 text-sm text-neutral-400">
            <option>Select store</option>
            <option>Dunelm, Oxford Street</option>
            <option>Dunelm, Westfield</option>
          </select>
        </div>
        <Input label="Date of purchase" type="date" required />
        <Input label="Amount spent" placeholder="£" type="number" required />
        <Button type="submit" fullWidth>Submit claim</Button>
      </form>
      <p className="text-xs text-neutral-400 text-center mt-3">Claims must be made within 7 days</p>
    </div>
  );
}
