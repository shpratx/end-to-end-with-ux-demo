import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { apiClient } from '../../lib/api-client';

const schema = z.object({ email: z.string().email('Email must be a valid email address') });
type FormData = z.infer<typeof schema>;

export function ResetPasswordPage() {
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    try {
      await apiClient.post('/auth/reset-password/request', data);
    } finally {
      setSubmitted(true);
      setLoading(false);
    }
  };

  if (submitted) {
    return (
      <div className="mx-auto max-w-[640px] py-12 px-4 text-center">
        <h1 className="font-display text-3xl font-bold text-primary-black mb-4">Check Your Email</h1>
        <p className="font-serif text-md text-neutral-600 mb-6">
          If an account exists with that email, a reset link has been sent.
        </p>
        <Link to="/auth/login" className="font-display text-sm text-accent-teal underline">
          Back to sign in
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <h1 className="font-display text-3xl font-bold text-primary-black mb-8">Reset Password</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <Input label="Email" type="email" {...register('email')} required error={errors.email?.message} />
        <Button type="submit" fullWidth loading={loading}>Send Reset Link</Button>
      </form>
      <p className="mt-6 text-center font-display text-sm text-neutral-500">
        <Link to="/auth/login" className="text-accent-teal underline">Back to sign in</Link>
      </p>
    </div>
  );
}
