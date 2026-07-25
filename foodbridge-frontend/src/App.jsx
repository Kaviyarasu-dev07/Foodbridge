import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { I18nextProvider } from 'react-i18next';
import i18n from './i18n';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DonorDashboard from './pages/DonorDashboard';
import NGODashboard from './pages/NGODashboard';
import AdminDashboard from './pages/AdminDashboard';
import Leaderboard from './pages/Leaderboard';
import ImpactCertificate from './pages/ImpactCertificate';

import './App.css';

// Guard component to enforce authentication and roles
const ProtectedRoute = ({ children, allowedRole }) => {
  const token = localStorage.getItem('token');
  const rawRole = localStorage.getItem('role');
  const role = rawRole ? rawRole.replace('ROLE_', '').trim().toUpperCase() : '';

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRole && role !== allowedRole.toUpperCase()) {
    if (role === 'DONOR') return <Navigate to="/donor/dashboard" replace />;
    if (role === 'NGO') return <Navigate to="/ngo/dashboard" replace />;
    if (role === 'ADMIN') return <Navigate to="/admin/dashboard" replace />;
    return <Navigate to="/" replace />;
  }

  return children;
};

function App() {
  const [installPrompt, setInstallPrompt] = useState(null);
  const [showBanner, setShowBanner] = useState(false);

  useEffect(() => {
    const handleBeforeInstallPrompt = (e) => {
      e.preventDefault();
      setInstallPrompt(e);

      // Check if dismissed in last 7 days
      const dismissedAt = localStorage.getItem('pwa_dismissed_at');
      if (dismissedAt) {
        const daysPassed = (Date.now() - parseInt(dismissedAt)) / (1000 * 60 * 60 * 24);
        if (daysPassed < 7) {
          return; // Still within 7 days grace period
        }
      }

      setShowBanner(true);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  const handleInstall = async () => {
    if (!installPrompt) return;
    installPrompt.prompt();
    const { outcome } = await installPrompt.userChoice;
    console.log(`User install choice: ${outcome}`);
    setInstallPrompt(null);
    setShowBanner(false);
  };

  const handleDismiss = () => {
    localStorage.setItem('pwa_dismissed_at', Date.now().toString());
    setShowBanner(false);
  };

  return (
    <I18nextProvider i18n={i18n}>
      <BrowserRouter>
        {showBanner && (
        <div className="pwa-install-banner">
          <div className="pwa-banner-content">
            <img src="/icons/icon-192.png" alt="FoodBridge Leaf" className="pwa-banner-icon" />
            <div>
              <h4>Add FoodBridge to home screen</h4>
              <p>Install for fast access, offline mode, and real-time food rescue push alerts.</p>
            </div>
          </div>
          <div className="pwa-banner-actions">
            <button onClick={handleInstall} className="btn btn-primary btn-small">Install</button>
            <button onClick={handleDismiss} className="btn btn-secondary btn-small">Dismiss</button>
          </div>
        </div>
      )}

      {/* Toast Notification Container */}
      <Toaster
        position="top-right"
        toastOptions={{
          success: {
            style: {
              background: '#e8f5e9',
              color: '#2e7d32',
              border: '1px solid #a5d6a7',
            },
          },
          error: {
            style: {
              background: '#ffebee',
              color: '#c62828',
              border: '1px solid #ffcdd2',
            },
          },
        }}
      />
      
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected Portals */}
        <Route
          path="/donor/dashboard"
          element={
            <ProtectedRoute allowedRole="DONOR">
              <DonorDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ngo/dashboard"
          element={
            <ProtectedRoute allowedRole="NGO">
              <NGODashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute allowedRole="ADMIN">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        
        {/* Leaderboard and Certificate Routes */}
        <Route path="/leaderboard" element={<Leaderboard />} />
        <Route
          path="/certificate"
          element={
            <ProtectedRoute>
              <ImpactCertificate />
            </ProtectedRoute>
          }
        />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
    </I18nextProvider>
  );
}

export default App;
