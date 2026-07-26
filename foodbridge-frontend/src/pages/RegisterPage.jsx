import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import Navbar from '../components/Navbar';

const RegisterPage = () => {
  const { t } = useTranslation();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('DONOR');
  const [phone, setPhone] = useState('');
  const [city, setCity] = useState('');
  const [latitude, setLatitude] = useState(13.0827);
  const [longitude, setLongitude] = useState(80.2707);
  const [verificationFile, setVerificationFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude);
          setLongitude(position.coords.longitude);
        },
        (err) => {
          console.warn("Geolocation permission denied, using Chennai coordinates as default.");
        }
      );
    }
  }, []);

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!name || !email || !password || !phone || !city) {
      toast.error("Please fill in all fields");
      return;
    }

    setLoading(true);
    try {
      const payload = {
        name,
        email,
        password,
        role,
        phone,
        city,
        latitude,
        longitude
      };
      const response = await api.post('/api/auth/register', payload);
      
      if (role === 'DONOR' && verificationFile) {
        try {
          const formData = new FormData();
          formData.append('document', verificationFile);
          await api.post(`/api/auth/${response.data.userId}/upload-verification`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
        } catch (uploadErr) {
          console.error("Failed to upload verification document:", uploadErr);
          toast.error("Account created, but document upload failed. Try again from dashboard later.");
        }
      }

      toast.success(response.data.message || "Registration successful! You can now log in.");
      navigate('/login');
    } catch (err) {
      console.error("Register error details:", err);
      let errMsg = "Registration failed. Please check your form details.";
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

  return (
    <div className="auth-container">
      <div className="auth-brand">
        <Link to="/" className="clean-logo-group" style={{ textDecoration: 'none' }}>
          <div className="clean-logo-box">🌿</div>
          <span className="clean-logo-text" style={{ fontSize: '1.5rem' }}>FoodBridge</span>
        </Link>
      </div>
      <div className="auth-card">
          <h2>{t('auth.registerTitle')}</h2>
          <p className="auth-subtitle">{t('auth.registerSubtitle', 'Register to begin sharing or claiming neighborhood excess food')}</p>
          <form onSubmit={handleRegister} className="auth-form">
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="name">{t('auth.name')}</label>
                <input
                  type="text"
                  id="name"
                  placeholder="e.g. John Doe / Helping Hand NGO"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="role">{t('auth.role')}</label>
                <select id="role" value={role} onChange={(e) => setRole(e.target.value)}>
                  <option value="DONOR">DONOR (Restaurant, Store, Individual)</option>
                  <option value="NGO">NGO (Charity, Food Bank)</option>
                  <option value="VOLUNTEER">VOLUNTEER (Delivery Driver)</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">{t('auth.email')}</label>
              <input
                type="email"
                id="email"
                placeholder="e.g. contact@example.com"
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
                placeholder="Min 6 characters"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="phone">{t('auth.phone')}</label>
                <input
                  type="tel"
                  id="phone"
                  placeholder="e.g. 9876543210"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="city">{t('auth.city')}</label>
                <input
                  type="text"
                  id="city"
                  placeholder="e.g. Chennai"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  required
                />
              </div>
            </div>

            {role === 'DONOR' && (
              <div className="form-group" style={{ marginTop: '15px' }}>
                <label htmlFor="verificationDoc">Verification Document (FSSAI/ID - Optional)</label>
                <input
                  type="file"
                  id="verificationDoc"
                  accept="image/jpeg, image/png, application/pdf"
                  onChange={(e) => setVerificationFile(e.target.files[0])}
                  style={{ padding: '8px 0' }}
                />
                <span className="info-subtext" style={{ fontSize: '0.8rem', color: '#666' }}>Optional. Max 5MB. Can be uploaded later in dashboard.</span>
              </div>
            )}

            <div className="form-coords-info">
              <p><strong>Detected Location:</strong> Lat: {latitude.toFixed(4)}, Lng: {longitude.toFixed(4)}</p>
              <span className="info-subtext">Coordinates are fetched via browser Geolocation to match nearby alerts.</span>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? t('common.loading') : t('auth.registerButton')}
            </button>
          </form>
          <div className="auth-footer">
            <p><Link to="/login">{t('auth.haveAccount')}</Link></p>
            <Link to="/" className="back-home">&larr; {t('nav.howItWorks')}</Link>
          </div>
      </div>
    </div>
  );
};

export default RegisterPage;
