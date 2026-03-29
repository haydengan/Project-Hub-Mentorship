import type { ReactNode } from 'react';
import './AuthLayout.css';

interface AuthLayoutProps {
  children: ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-layout">
      <div className="auth-left">
        <div className="auth-left-content">
          <h1 className="auth-brand">Togetherly</h1>
          <p className="auth-tagline">Stay on track, together.</p>
        </div>
        <div className="auth-decor">
          <div className="decor-ring decor-ring-1" />
          <div className="decor-ring decor-ring-2" />
          <div className="decor-dot decor-dot-1" />
          <div className="decor-dot decor-dot-2" />
          <div className="decor-dot decor-dot-3" />
        </div>
      </div>
      <div className="auth-right">
        {children}
      </div>
    </div>
  );
}
