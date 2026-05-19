import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useLogin } from '../../hooks/useAuth';

const loginSchema = z.object({
  email: z.string().email('Email must be a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const { mutate: login, isPending, error } = useLogin();
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormData) => {
    login(data, { onSuccess: () => navigate('/home') });
  };

  return (
    <div className="mx-auto max-w-[640px] py-12 px-4">
      <h1 className="font-display text-3xl font-bold text-primary-black mb-8">Sign In</h1>

      {error && (
        <div className="mb-6 rounded-md bg-error/10 p-4 text-sm text-error" role="alert">
          {error.message}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
        <Input label="Email" type="email" {...register('email')} required error={errors.email?.message} />
        <Input label="Password" type="password" {...register('password')} required error={errors.password?.message} />

        <div className="flex justify-end">
          <Link to="/auth/reset-password" className="font-display text-sm text-accent-teal underline">
            Forgot password?
          </Link>
        </div>

        <Button type="submit" fullWidth loading={isPending}>
          Sign In
        </Button>
      </form>

      <div className="my-8 flex items-center gap-4">
        <hr className="flex-1 border-neutral-200" />
        <span className="font-display text-sm text-neutral-400">or</span>
        <hr className="flex-1 border-neutral-200" />
      </div>

      <div className="flex flex-col gap-3">
        <Button variant="secondary" fullWidth type="button" aria-label="Continue with Google" onClick={() => alert('Google Sign-In would open here')}>
          Continue with Google
        </Button>
        <Button variant="secondary" fullWidth type="button" aria-label="Continue with Apple" onClick={() => alert('Apple Sign-In would open here')}>
          Continue with Apple
        </Button>
      </div>

      <p className="mt-6 text-center font-display text-sm text-neutral-500">
        Don't have an account? <Link to="/auth/register" className="text-accent-teal underline">Join now</Link>
      </p>
    </div>
  );
}
