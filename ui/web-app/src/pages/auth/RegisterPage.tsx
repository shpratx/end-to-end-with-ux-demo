import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useRegister } from '../../hooks/useAuth';

const registerSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  phone: z.string().optional(),
  password: z.string().min(8, 'Minimum 8 characters'),
  dataConsent: z.literal(true, { errorMap: () => ({ message: 'You must accept to continue' }) }),
  marketingConsent: z.boolean().optional(),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const [step, setStep] = useState<'landing' | 'overview' | 'privacy' | 'form'>('landing');
  const navigate = useNavigate();
  const { mutate: register, isPending, error } = useRegister();
  const { register: field, handleSubmit, formState: { errors, isValid, submitCount } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: 'onBlur',
  });

  const onSubmit = (data: RegisterFormData) => {
    register(
      { name: '', email: data.email, phone: data.phone ?? '', password: data.password, termsAndConditionsVersion: 'v1.0' },
      { onSuccess: (res) => navigate('/auth/verify', { state: { customerId: res.customerId } }) },
    );
  };

  // Screen 1.0: Loyalty Landing Page
  if (step === 'landing') {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4">
        <div className="bg-primary-black rounded-sm p-8 text-white mb-8">
          <p className="text-xs font-semibold uppercase tracking-wide text-[#007A7A] mb-2">Next Loyalty</p>
          <h1 className="font-display text-3xl font-bold mb-2">Earn rewards every time you shop</h1>
          <p className="text-sm text-neutral-300">No credit account needed. Open to every Next customer.</p>
        </div>

        <div className="flex flex-col gap-4 mb-8">
          <div className="flex items-start gap-3">
            <span className="w-8 h-8 bg-[#007A7A]/10 rounded flex items-center justify-center text-[#007A7A] font-bold text-sm">★</span>
            <div><p className="font-display text-sm font-semibold text-primary-black">Earn points</p><p className="text-sm text-neutral-500">on every purchase — online, in-store, Click &amp; Collect</p></div>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-8 h-8 bg-[#007A7A]/10 rounded flex items-center justify-center text-[#007A7A] font-bold text-sm">⚡</span>
            <div><p className="font-display text-sm font-semibold text-primary-black">Early sale access</p><p className="text-sm text-neutral-500">earn your way into the Next Sale before anyone else</p></div>
          </div>
          <div className="flex items-start gap-3">
            <span className="w-8 h-8 bg-[#007A7A]/10 rounded flex items-center justify-center text-[#007A7A] font-bold text-sm">✓</span>
            <div><p className="font-display text-sm font-semibold text-primary-black">In-store recognition</p><p className="text-sm text-neutral-500">your loyalty shows at the till</p></div>
          </div>
        </div>

        <Button fullWidth onClick={() => setStep('overview')}>Join the Loyalty Programme</Button>
        <Link to="/auth/login"><Button variant="secondary" fullWidth className="mt-3" type="button">Sign in to existing account</Button></Link>
        <button onClick={() => setStep('overview')} className="block mx-auto mt-3 text-xs text-neutral-400 underline">Learn how it works ↓</button>
      </div>
    );
  }

  // Screen 2.0: Programme Overview
  if (step === 'overview') {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4">
        <button onClick={() => setStep('landing')} className="font-display text-sm text-[#007A7A] mb-6" aria-label="Go back">← Back</button>
        <h1 className="font-display text-2xl font-bold mb-6">How it works</h1>

        <div className="bg-neutral-50 rounded-sm p-6 mb-6">
          <p className="font-display text-sm font-semibold uppercase tracking-wide text-neutral-500 mb-2">Earn</p>
          <p className="font-display text-lg font-bold text-primary-black">Spend £1 · Earn 5 points · 500 points = £5 reward</p>
          <p className="text-sm text-neutral-600 mt-1">Every purchase counts. Online, in-store, Click &amp; Collect.</p>
        </div>

        <div className="mb-6">
          <p className="font-display text-sm font-semibold uppercase tracking-wide text-neutral-500 mb-3">Tiers</p>
          <div className="flex flex-col gap-3">
            <div className="flex items-center gap-3"><span className="w-8 h-8 bg-neutral-200 rounded-full flex items-center justify-center font-display text-xs font-bold">M</span><div><p className="font-display text-sm font-semibold">Member</p><p className="text-xs text-neutral-500">earn points on every purchase</p></div></div>
            <div className="flex items-center gap-3"><span className="w-8 h-8 bg-neutral-300 rounded-full flex items-center justify-center font-display text-xs font-bold">S</span><div><p className="font-display text-sm font-semibold">Silver</p><p className="text-xs text-neutral-500">early sale access + bonus earn events</p></div></div>
            <div className="flex items-center gap-3"><span className="w-8 h-8 bg-amber-200 rounded-full flex items-center justify-center font-display text-xs font-bold">G</span><div><p className="font-display text-sm font-semibold">Gold</p><p className="text-xs text-neutral-500">extended sale access + exclusive collections</p></div></div>
          </div>
        </div>

        <div className="bg-neutral-50 rounded-sm p-4 mb-8">
          <p className="text-sm text-neutral-700"><strong>Physical card available</strong> — Don't have a smartphone? Request a loyalty card — works at every Next till.</p>
        </div>

        <Button fullWidth onClick={() => setStep('privacy')}>Continue to sign up</Button>
      </div>
    );
  }

  // Screen 3.0: Data Use Statement
  if (step === 'privacy') {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4">
        <button onClick={() => setStep('overview')} className="font-display text-sm text-[#007A7A] mb-6" aria-label="Go back">← Back</button>
        <h1 className="font-display text-2xl font-bold mb-2">Your data &amp; privacy</h1>
        <p className="text-sm text-neutral-600 mb-6">How we use your loyalty data</p>

        <p className="text-sm text-neutral-700 mb-6">We track your purchases to give you rewards and recognise you as a loyal customer. We do not use your loyalty data to restrict your account or penalise returns.</p>

        <div className="mb-6">
          <p className="font-display text-xs font-semibold uppercase tracking-wide text-neutral-500 mb-3">What we track</p>
          <div className="flex flex-col gap-2">
            <p className="text-sm text-neutral-700">✓ Purchases — to calculate and credit your points</p>
            <p className="text-sm text-neutral-700">✓ Loyalty account activity — to maintain your balance and tier</p>
          </div>
        </div>

        <div className="mb-8">
          <p className="font-display text-xs font-semibold uppercase tracking-wide text-neutral-500 mb-3">What we don't do</p>
          <div className="flex flex-col gap-2">
            <p className="text-sm text-neutral-700">✗ Use your loyalty data to restrict your account</p>
            <p className="text-sm text-neutral-700">✗ Share your data with third parties for advertising</p>
          </div>
        </div>

        <Button fullWidth onClick={() => setStep('form')}>I Understand — Continue</Button>
        <p className="text-center mt-4 text-xs text-neutral-400 underline cursor-pointer">Read full privacy policy</p>
      </div>
    );
  }

  // Screen 4.0: Enrolment Form
  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <button onClick={() => setStep('privacy')} className="font-display text-sm text-[#007A7A] mb-6" aria-label="Go back">← Back</button>
      <h1 className="font-display text-2xl font-bold mb-6">Create your account</h1>

      {error && (
        <div className="mb-6 rounded-md bg-red-50 border border-red-200 p-4 text-sm text-red-700" role="alert">{error.message}</div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <Input label="Email address" type="email" placeholder="your@email.com" {...field('email')} required error={errors.email?.message} />
        <div>
          <Input label="Mobile number (recommended)" type="tel" placeholder="+44 7700 000000" {...field('phone')} />
          <p className="text-xs text-neutral-400 mt-1">Adding your mobile means we can find your account at the till even if you forget your email.</p>
        </div>
        <Input label="Create a password" type="password" placeholder="Min. 8 characters" {...field('password')} required error={errors.password?.message} />
        <p className="text-center text-xs text-neutral-400 my-2">— Or —</p>
        <p className="text-xs text-neutral-500 text-center">continue with a <span className="text-[#007A7A] font-semibold">magic link</span> — we'll email you a sign-in link instead</p>

        <div className="border-t border-neutral-200 pt-4 mt-2">
          <div className="flex items-start gap-2 mb-3">
            <input type="checkbox" id="dataConsent" {...field('dataConsent')} className="mt-1 w-4 h-4 accent-[#007A7A]" />
            <label htmlFor="dataConsent" className="text-xs text-neutral-600">I understand how Next will use my loyalty data</label>
          </div>
          {errors.dataConsent && <p className="text-xs text-red-600 mb-2">{errors.dataConsent.message}</p>}

          <div className="flex items-start gap-2">
            <input type="checkbox" id="marketingConsent" {...field('marketingConsent')} className="mt-1 w-4 h-4 accent-[#007A7A]" />
            <label htmlFor="marketingConsent" className="text-xs text-neutral-600">I'd like to receive personalised sale and offer emails (optional)</label>
          </div>
        </div>

        <Button type="submit" fullWidth loading={isPending} disabled={!isValid && submitCount > 0} className="mt-4">Create my loyalty account</Button>
      </form>
    </div>
  );
}
