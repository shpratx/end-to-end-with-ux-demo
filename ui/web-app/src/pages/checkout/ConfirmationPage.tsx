import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';

export function ConfirmationPage() {
  return (
    <div className="mx-auto max-w-[640px] py-12 px-4 text-center">
      <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <span className="text-xl">✓</span>
      </div>
      <h1 className="font-display text-2xl font-bold mb-1">Order confirmed</h1>
      <p className="text-sm text-neutral-500 mb-6">Order #NXT-2026-04821</p>

      <div className="bg-[#0A8A00]/5 border border-[#0A8A00]/20 rounded-sm p-4 mb-6 text-center">
        <p className="font-display text-sm font-bold text-[#0A8A00]">★ 505 points earned!</p>
        <p className="text-xs text-neutral-600 mt-1">Worth £5.05 · Your balance: 605 pts</p>
      </div>

      <div className="text-left text-sm text-neutral-600 leading-relaxed mb-6">
        <p>Delivery: Click & Collect</p>
        <p>Store: Dunelm, Oxford Street</p>
        <p>Ready by: Tomorrow</p>
      </div>

      <Link to="/home"><Button fullWidth>View my loyalty dashboard</Button></Link>
      <Link to="/" className="block mt-3 font-display text-sm text-[#0A8A00] underline">Continue shopping</Link>
    </div>
  );
}
