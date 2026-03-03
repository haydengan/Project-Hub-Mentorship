import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import client from '../api/client';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await client.post('/api/auth/forgetPassword', { email });
      setSent(true);
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
        setError('Something went wrong. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  if (sent) {
    return (
      <AuthLayout>
        <div className="auth-form">
          <h2>Check your email</h2>
          <p className="subtitle">
            We've sent a password reset link to {email}. Please check your inbox and click the link to reset your password.
          </p>
          <Link to="/login" className="auth-link" style={{ display: 'block', marginTop: '1.5rem' }}>
            Back to Sign In
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Forgot password</h2>
        <p className="subtitle">Please enter your email to reset the password</p>

        {error && <p className="error-message">{error}</p>}

        <label htmlFor="email">Your Email</label>
        <input
          id="email"
          type="email"
          placeholder="Enter your email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <button type="submit" className="btn-primary btn-coral" disabled={loading}>
          {loading ? 'Sending...' : 'Reset Password'}
        </button>
      </form>
    </AuthLayout>
  );
}
