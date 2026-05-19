import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuthStore } from '../../lib/auth-store';

export function PaymentPage() {
  const [showEnrolment, setShowEnrolment] = useState(false);
  const [dismissed, setDismissed] = useState(false);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const navigate = useNavigate();
  const orderTotal = 101;
  const pointsEarnable = orderTotal * 5;

  if (showEnrolment) {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4">
        <button onClick={() => setShowEnrolment(false)} className="font-display text-sm text-[#007A7A] mb-6">← Back to payment</button>
        <h1 className="font-display text-xl font-bold mb-4">Join Next Loyalty</h1>
        <div className="bg-neutral-50 border border-neutral-200 rounded-sm p-4 mb-4">
          <p className="text-xs text-neutral-600 leading-relaxed">We track purchases to give you rewards. We do not use data to restrict your account.</p>
          <div className="mt-3 flex flex-col gap-2">
            <label className="flex items-start gap-2 text-xs text-neutral-600"><input type="checkbox" className="mt-0.5 accent-[#007A7A]" /> I understand</label>
            <label className="flex items-start gap-2 text-xs text-neutral-600"><input type="checkbox" className="mt-0.5 accent-[#007A7A]" /> Send me offers (optional)</label>
          </div>
        </div>
        <Input label="Email address" defaultValue="clara@example.com" />
        <p className="text-xs text-neutral-400 mt-1 mb-3">Pre-filled from checkout</p>
        <Input label="Mobile number (optional)" placeholder="+44 7700 000000" />
        <Button fullWidth className="mt-4" onClick={() => navigate('/checkout/confirmation')}>Confirm & earn points</Button>
        <p className="text-xs text-neutral-400 text-center mt-3">No password needed now — we'll email you a sign-in link.</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <Link to="/checkout/basket" className="font-display text-sm text-[#007A7A] mb-6 block">← Back to basket</Link>
      <h1 className="font-display text-xl font-bold mb-2">Payment</h1>
      <p className="text-sm text-neutral-500 mb-6">Order total: £{orderTotal.toFixed(2)}</p>

      <Input label="Card number" placeholder="•••• •••• •••• ••••" />
      <div className="flex gap-3 mt-3">
        <div className="flex-1"><Input label="Expiry" placeholder="MM/YY" /></div>
        <div className="flex-1"><Input label="CVV" placeholder="•••" /></div>
      </div>

      {!isAuthenticated && !dismissed && (
        <div className="bg-[#007A7A]/5 border border-[#007A7A]/20 rounded-sm p-4 mt-6">
          <p className="font-display text-sm font-bold text-[#007A7A]">★ You'd earn {pointsEarnable} points (worth £{(pointsEarnable / 100).toFixed(2)}) on this order</p>
          <div className="flex gap-2 mt-3">
            <button onClick={() => setShowEnrolment(true)} className="flex-1 bg-[#007A7A] text-white border-none rounded py-2 text-xs font-bold cursor-pointer">Join Now</button>
            <button className="flex-1 bg-white text-neutral-500 border border-neutral-200 rounded py-2 text-xs cursor-pointer" onClick={() => setDismissed(true)}>Skip</button>
          </div>
        </div>
      )}

      <Button fullWidth className="mt-6" onClick={() => navigate('/checkout/confirmation')}>Pay £{orderTotal.toFixed(2)}</Button>
    </div>
  );
}
