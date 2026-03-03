import { useNavigate } from 'react-router-dom';
import client from '../api/client';

export default function HomePage() {
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      await client.post('/api/auth/logout');
    } finally {
      navigate('/login');
    }
  }

  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>Welcome to Togetherly</h1>
      <p>You are logged in.</p>
      <button onClick={handleLogout} style={{ marginTop: '1rem', padding: '0.5rem 1.5rem', cursor: 'pointer' }}>
        Logout
      </button>
    </div>
  );
}
