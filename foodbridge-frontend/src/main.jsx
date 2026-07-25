import React, { StrictMode, Component } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// Automatically unregister any conflicting PWA service workers from dev mode
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    for (let registration of registrations) {
      registration.unregister();
      console.log('Unregistered conflicting service worker.');
    }
  }).catch(err => console.error(err));
}

// Global Error Boundary to prevent blank white screens
class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught an error", error, errorInfo);
  }

  handleReset = () => {
    localStorage.clear();
    sessionStorage.clear();
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '40px', fontFamily: 'sans-serif', textAlign: 'center', background: '#fffbee', color: '#333', minHeight: '100vh' }}>
          <h2 style={{ color: '#c62828' }}>⚠️ Something went wrong</h2>
          <p style={{ fontSize: '16px', marginBottom: '20px' }}>
            {this.state.error?.toString() || "A rendering error occurred in the application."}
          </p>
          <button 
            onClick={this.handleReset}
            style={{ padding: '10px 20px', background: '#1D9E75', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}
          >
            🔄 Clear Cache & Reload
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ErrorBoundary>
      <React.Suspense fallback={<div style={{padding: '40px', textAlign: 'center', fontFamily: 'sans-serif', fontSize: '18px', color: '#1D9E75'}}>⏳ Loading FoodBridge Portal...</div>}>
        <App />
      </React.Suspense>
    </ErrorBoundary>
  </StrictMode>,
)
