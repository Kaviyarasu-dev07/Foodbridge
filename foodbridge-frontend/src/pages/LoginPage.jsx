import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import Navbar from '../components/Navbar';

const LoginPage = () => {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error("Please fill in all fields");
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/api/auth/login', { email, password });
      const { token, role, userId } = response.data;
      const cleanRole = role ? role.replace('ROLE_', '').trim().toUpperCase() : '';

      localStorage.setItem('token', token);
      localStorage.setItem('role', cleanRole);
      if (userId) localStorage.setItem('userId', userId.toString());
      localStorage.setItem('userEmail', email);

      toast.success(t('auth.welcome', 'Welcome back!'));

      // Redirect by role
      if (cleanRole === 'DONOR') {
        navigate('/donor/dashboard', { replace: true });
      } else if (cleanRole === 'NGO') {
        navigate('/ngo/dashboard', { replace: true });
      } else if (cleanRole === 'ADMIN') {
        navigate('/admin/dashboard', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      console.error("Login error details:", err);
      let errMsg = "Login failed. Please check your email and password.";
      if (typeof err.response?.data === 'string') {
        errMsg = err.response.data;
      } else if (err.response?.data?.error) {
        errMsg = err.response.data.error;
      } else if (err.response?.data?.message) {
        errMsg = err.response.data.message;
      } else if (err.code === 'ERR_NETWORK' || !err.response) {
        errMsg = 'Cannot connect to backend server. Please make sure Spring Boot is running on http://localhost:8080';
      } else if (err.message) {
        errMsg = err.message;
      }
      toast.error(errMsg);
    } finally {
      setLoading(false);
    }
  };

  const fillDemoAccount = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    toast.success(`Filled ${demoEmail}`);
  };

  return (
    <div className="auth-container">
      <div className="auth-brand">
        <Link to="/" className="clean-logo-group" style={{ textDecoration: 'none' }}>
          <div className="clean-logo-box">🌿</div>
          <span className="clean-logo-text" style={{ fontSize: '1.5rem' }}>FoodBridge</span>
        </Link>
      </div>
      <div className="auth-card">
          <h2>{t('auth.loginTitle')}</h2>
          <p className="auth-subtitle">{t('auth.loginSubtitle', 'Enter your registered details to access your portal')}</p>
          
          <div className="demo-accounts-box" style={{ marginBottom: '1.25rem', padding: '0.75rem', background: 'rgba(29, 158, 117, 0.05)', borderRadius: '8px', border: '1px dashed var(--primary)' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: '600', color: 'var(--primary)', display: 'block', marginBottom: '0.5rem' }}>⚡ Quick Demo Accounts:</span>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <button type="button" className="btn btn-secondary btn-small" onClick={() => fillDemoAccount('donor@foodbridge.com', 'password123')}>
                Donor Demo
              </button>
              <button type="button" className="btn btn-secondary btn-small" onClick={() => fillDemoAccount('ngo@foodbridge.com', 'password123')}>
                NGO Demo
              </button>
              <button type="button" className="btn btn-secondary btn-small" onClick={() => fillDemoAccount('admin@foodbridge.com', 'password123')}>
                Admin Demo
              </button>
            </div>
          </div>

          <form onSubmit={handleLogin} className="auth-form">
            <div className="form-group">
              <label htmlFor="email">{t('auth.email')}</label>
              <input
                type="email"
                id="email"
                placeholder="e.g. donor@foodbridge.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">{t('auth.password')}</label>
              <input
                type="password"
                id="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? t('common.loading') : t('auth.loginButton')}
            </button>
          </form>
          <div className="auth-footer">
            <p><Link to="/register">{t('auth.noAccount')}</Link></p>
            <Link to="/" className="back-home">&larr; {t('nav.howItWorks')}</Link>
          </div>
      </div>
    </div>
  );
};

export default LoginPage;
