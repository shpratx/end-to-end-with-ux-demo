import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Input } from './Input';

describe('Input', () => {
  it('renders label', () => {
    render(<Input label="Email" />);
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
  });

  it('shows error message and sets aria-invalid', () => {
    render(<Input label="Email" error="Email is required" />);
    const input = screen.getByLabelText('Email');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(screen.getByRole('alert')).toHaveTextContent('Email is required');
  });

  it('shows helper text when no error', () => {
    render(<Input label="Password" helperText="Min 12 chars" />);
    expect(screen.getByText('Min 12 chars')).toBeInTheDocument();
  });

  it('does not show helper text when error is present', () => {
    render(<Input label="Password" helperText="Min 12 chars" error="Too short" />);
    expect(screen.queryByText('Min 12 chars')).not.toBeInTheDocument();
    expect(screen.getByText('Too short')).toBeInTheDocument();
  });

  it('marks required fields with asterisk', () => {
    render(<Input label="Name" required />);
    expect(screen.getByText('*')).toBeInTheDocument();
  });
});
