import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { joinGroup } from '../api/groups';
import AuthLayout from '../components/AuthLayout';
import '../components/AuthLayout.css';

export default function JoinGroupPage() {
  const navigate = useNavigate();
  const [inviteCode, setInviteCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const group = await joinGroup(inviteCode);
      navigate(`/groups/${group.id}`);
    } catch (err: unknown) {
      if (
        err !== null && typeof err === 'object' && 'response' in err &&
        err.response !== null && typeof err.response === 'object' &&
        'data' in err.response && err.response.data !== null &&
        typeof err.response.data === 'object' && 'message' in err.response.data
      ) {
        setError(String((err.response as { data: { message: string } }).data.message));
      } else {
        setError('Failed to join group. Check the invite code.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Join a Group</h2>
        <p className="subtitle">Enter the invite code shared by your group admin.</p>

        {error && <p className="error-message">{error}</p>}

        <label htmlFor="code">Invite Code</label>
        <input
          id="code"
          type="text"
          placeholder="e.g. a1b2c3d4"
          value={inviteCode}
          onChange={e => setInviteCode(e.target.value)}
          required
        />

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Joining...' : 'Join Group'}
        </button>

        <p className="auth-link" style={{ marginTop: '1rem' }}>
          <a href="/" onClick={e => { e.preventDefault(); navigate('/'); }}>
            Back to Dashboard
          </a>
        </p>
      </form>
    </AuthLayout>
  );
}
