import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';

const mockBasket = [
  { id: '1', name: 'Navy Wool Coat', price: 89, size: '12', qty: 1 },
  { id: '2', name: 'White T-Shirt', price: 12, size: 'M', qty: 1 },
];

export function BasketPage() {
  const subtotal = mockBasket.reduce((sum, item) => sum + item.price * item.qty, 0);

  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <h1 className="font-display text-2xl font-bold mb-6">Your Basket</h1>

      <div className="flex flex-col gap-3 mb-6">
        {mockBasket.map((item) => (
          <div key={item.id} className="border border-neutral-200 rounded-sm p-4">
            <div className="flex justify-between">
              <span className="font-display text-sm font-semibold">{item.name}</span>
              <span className="font-display text-sm font-bold">£{item.price.toFixed(2)}</span>
            </div>
            <p className="text-xs text-neutral-400 mt-1">Size: {item.size} · Qty: {item.qty}</p>
          </div>
        ))}
      </div>

      <div className="flex flex-col gap-1 text-sm mb-2">
        <div className="flex justify-between"><span>Subtotal</span><span>£{subtotal.toFixed(2)}</span></div>
        <div className="flex justify-between text-[#1A7A4A]"><span>Delivery (Click & Collect)</span><span>FREE</span></div>
      </div>
      <div className="border-t border-neutral-200 pt-2 mb-6">
        <div className="flex justify-between font-display text-base font-bold"><span>Total</span><span>£{subtotal.toFixed(2)}</span></div>
      </div>

      <Link to="/checkout/payment">
        <Button fullWidth>Continue to payment</Button>
      </Link>
    </div>
  );
}
