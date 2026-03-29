import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import './LoginPage.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await client.post('/web/api/auth/login', { username, password });
      navigate('/');
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
        setError('Login failed. Please check your credentials.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-hero">
        <div className="hero-content">
          <h1 className="hero-brand">Togetherly</h1>
          <p className="hero-tagline">Build habits together.<br />Stay accountable, stay consistent.</p>
        </div>
        <div className="hero-shapes">
          <div className="shape shape-1" />
          <div className="shape shape-2" />
          <div className="shape shape-3" />
          <div className="shape shape-4" />
        </div>
      </div>

      <div className="login-form-side">
        <form className="login-form" onSubmit={handleSubmit}>
          <h2>Welcome back</h2>
          <p className="login-subtitle">Sign in to your account</p>

          {error && <div className="form-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="username">Email or username</label>
            <input
              id="username"
              type="text"
              placeholder="Enter your email or username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <div className="label-row">
              <label htmlFor="password">Password</label>
              <Link to="/forgot-password" className="forgot-link">Forgot?</Link>
            </div>
            <input
              id="password"
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>

          <button type="submit" className="btn-submit" disabled={loading}>
            {loading ? (
              <span className="btn-loading">
                <span className="spinner" />
                Signing in...
              </span>
            ) : 'Sign In'}
          </button>

          <p className="form-footer">
            Don't have an account? <Link to="/signup">Create one</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
