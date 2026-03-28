import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createGroup } from '../api/groups';
import AuthLayout from '../components/AuthLayout';
import '../components/AuthLayout.css';

export default function CreateGroupPage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const group = await createGroup(name, description);
      navigate(`/groups/${group.id}`);
    } catch {
      setError('Failed to create group.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Create a Group</h2>
        <p className="subtitle">Start an accountability group and invite your friends.</p>

        {error && <p className="error-message">{error}</p>}

        <label htmlFor="name">Group Name</label>
        <input
          id="name"
          type="text"
          placeholder="e.g. Morning Gym Crew"
          value={name}
          onChange={e => setName(e.target.value)}
          maxLength={64}
          required
        />

        <label htmlFor="desc">Description (optional)</label>
        <input
          id="desc"
          type="text"
          placeholder="What's this group about?"
          value={description}
          onChange={e => setDescription(e.target.value)}
          maxLength={256}
        />

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Creating...' : 'Create Group'}
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
