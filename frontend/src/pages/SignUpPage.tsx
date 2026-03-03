import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import client from '../api/client';

export default function SignUpPage() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [agreed, setAgreed] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    if (!agreed) {
      setError('You must agree to the Terms and Conditions.');
      return;
    }

    setLoading(true);
    try {
      const res = await client.post('/api/auth/issueVerificationCode', { email });
      const verificationKey: string = res.data.key;

      navigate('/verify-code', {
        state: {
          username: fullName,
          email,
          password,
          verificationKey,
        },
      });
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

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Create your account</h2>
        <p className="subtitle">It's free and easy</p>

        {error && <p className="error-message">{error}</p>}

        <label htmlFor="fullName">Full name</label>
        <input
          id="fullName"
          type="text"
          placeholder="Enter your name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
        />

        <label htmlFor="email">E-mail or phone number</label>
        <input
          id="email"
          type="email"
          placeholder="Type your e-mail or phone number"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          placeholder="Type your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <p className="hint">Must be 8 characters at least</p>

        <label className="checkbox-label" style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem', fontSize: '0.8rem', color: '#666', marginBottom: '1.2rem', cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            style={{ marginTop: '0.2rem' }}
          />
          <span>
            By creating an account means you agree to the{' '}
            <a href="#" style={{ color: '#1a1a1a', fontWeight: 700 }}>Terms and Conditions</a>, and our{' '}
            <a href="#" style={{ color: '#1a1a1a', fontWeight: 700 }}>Privacy Policy</a>
          </span>
        </label>

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Creating account...' : 'Sign Up'}
        </button>

        <div className="divider">
          <span>or do it via other accounts</span>
        </div>

        <div className="social-icons">
          <div className="social-icon">G</div>
          <div className="social-icon">&apple;</div>
          <div className="social-icon">f</div>
        </div>

        <p className="auth-link">
          Already have an account? <Link to="/login">Sign In</Link>
        </p>
      </form>
    </AuthLayout>
  );
}
