import { Link } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';

export default function SuccessPage() {
  return (
    <AuthLayout>
      <div className="auth-form" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>&#9989;</div>
        <h2 style={{ marginBottom: '0.5rem' }}>Successful</h2>
        <p className="subtitle">
          Congratulations! Your password has been changed. Click continue to login.
        </p>
        <Link to="/login" className="btn-primary btn-green" style={{ display: 'block', textDecoration: 'none', padding: '0.75rem', borderRadius: '24px', textAlign: 'center', marginTop: '1.5rem' }}>
          Continue
        </Link>
      </div>
    </AuthLayout>
  );
}
