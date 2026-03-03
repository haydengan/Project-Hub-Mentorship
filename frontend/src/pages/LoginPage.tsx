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
    <div className="login-wrapper">
      <h1 className="brand-title">Togetherly</h1>

      <div className="login-card-container">
        <div className="card-bg card-bg-1" />
        <div className="card-bg card-bg-2" />

        <form className="login-card" onSubmit={handleSubmit}>
          <h2>Welcome back!</h2>

          {error && <p className="error-message">{error}</p>}

          <label htmlFor="username">E-mail or username</label>
          <input
            id="username"
            type="text"
            placeholder="Type your e-mail or username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
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
          <Link to="/forgot-password" className="forgot-link">Forgot Password?</Link>

          <button type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

          <div className="divider">
            <span>or do it via other accounts</span>
          </div>

          <div className="social-icons">
            <div className="social-icon">Gmail</div>
            <div className="social-icon">Apple</div>
          </div>

          <p className="signup-prompt">
            Don't have an account? <Link to="/signup">Sign Up</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
