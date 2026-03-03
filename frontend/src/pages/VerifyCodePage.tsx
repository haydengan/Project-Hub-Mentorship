import { useState, useRef, type FormEvent, type KeyboardEvent } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import client from '../api/client';
import './VerifyCodePage.css';

interface VerifyState {
  username: string;
  email: string;
  password: string;
  verificationKey: string;
}

export default function VerifyCodePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as VerifyState | null;

  const [digits, setDigits] = useState(['', '', '', '', '']);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [verificationKey, setVerificationKey] = useState(state?.verificationKey ?? '');
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  if (!state) {
    return <Navigate to="/signup" replace />;
  }

  const { username, email, password } = state;

  function handleDigitChange(index: number, value: string) {
    if (value.length > 1) value = value[value.length - 1];
    if (value && !/^\d$/.test(value)) return;

    const next = [...digits];
    next[index] = value;
    setDigits(next);

    if (value && index < 4) {
      inputRefs.current[index + 1]?.focus();
    }
  }

  function handleKeyDown(index: number, e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Backspace' && !digits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const code = digits.join('');
    if (code.length < 5) {
      setError('Please enter all 5 digits.');
      return;
    }

    setError('');
    setLoading(true);
    try {
      await client.post('/api/auth/register', {
        username,
        email,
        password,
        verification: { key: verificationKey, code },
      });
      navigate('/login', { state: { registered: true } });
    } catch (err: unknown) {
      if (
        err !== null &&
        typeof err === 'object' &&
        'response' in err &&
        err.response !== null &&
        typeof err.response === 'object' &&
        'data' in err.response &&
        err.response.data !== null &&
        typeof err.response.data === 'object' &&
        'message' in err.response.data
      ) {
        setError(String((err.response as { data: { message: string } }).data.message));
      } else {
        setError('Verification failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleResend() {
    setResending(true);
    setError('');
    try {
      const res = await client.post('/api/auth/issueVerificationCode', { email });
      setVerificationKey(res.data.key);
      setDigits(['', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } catch {
      setError('Failed to resend code. Please try again.');
    } finally {
      setResending(false);
    }
  }

  const maskedEmail = email.replace(/(.{3}).*(@.*)/, '$1...$2');

  return (
    <AuthLayout>
      <form className="auth-form verify-code-form" onSubmit={handleSubmit}>
        <h2>Check your email</h2>
        <p className="subtitle">
          We sent a code to {maskedEmail}<br />
          Enter the 5 digit code mentioned in the email
        </p>

        {error && <p className="error-message">{error}</p>}

        <div className="code-inputs">
          {digits.map((digit, i) => (
            <input
              key={i}
              ref={(el) => { inputRefs.current[i] = el; }}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleDigitChange(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              className="code-digit"
            />
          ))}
        </div>

        <button type="submit" className="btn-primary btn-coral" disabled={loading}>
          {loading ? 'Verifying...' : 'Verify Code'}
        </button>

        <p className="auth-link" style={{ marginTop: '1.2rem' }}>
          Haven't got the email yet?{' '}
          <a href="#" onClick={(e) => { e.preventDefault(); handleResend(); }}>
            {resending ? 'Sending...' : 'Resend email'}
          </a>
        </p>
      </form>
    </AuthLayout>
  );
}
