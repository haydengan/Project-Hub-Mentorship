import type { ReactNode } from 'react';
import './AuthLayout.css';

interface AuthLayoutProps {
  children: ReactNode;
}

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-layout">
      <div className="auth-left">
        <h1 className="auth-brand">Togetherly</h1>
      </div>
      <div className="auth-right">
        {children}
      </div>
    </div>
  );
}
