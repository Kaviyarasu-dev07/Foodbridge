import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import PhotoEstimator from '../components/PhotoEstimator';
import ThemeToggle from '../components/ThemeToggle';
import RescueChat from '../components/RescueChat';

const DonorDashboard = () => {
  const { t } = useTranslation();
  const [profile, setProfile] = useState(null);
  const [listings, setListings] = useState([]);
  const [foodName, setFoodName] = useState('');
  const [quantity, setQuantity] = useState('');
  const [foodType, setFoodType] = useState('COOKED_MEAL');
  const [condition, setCondition] = useState('FRESH');
  const [location, setLocation] = useState('');
  const [pickupMinutes, setPickupMinutes] = useState(60);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [viewingQR, setViewingQR] = useState(null); // stores QR base64 string
  const [showEstimator, setShowEstimator] = useState(false);
  const [aiEstimated, setAiEstimated] = useState(false);
  const [activeChatListing, setActiveChatListing] = useState(null);
  const [unreadCounts, setUnreadCounts] = useState({});
  const [averageRating, setAverageRating] = useState(null);
  const [ratingListing, setRatingListing] = useState(null);
  const [ratingScore, setRatingScore] = useState(5);
  const [ratingComment, setRatingComment] = useState('');
  const [docFile, setDocFile] = useState(null);
  const [uploadingDoc, setUploadingDoc] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchProfile();
    fetchListings();
    fetchUnreadCounts();
    const interval = setInterval(fetchUnreadCounts, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/api/auth/me');
      setProfile(response.data);
      if (!location && response.data.city) {
        setLocation(`${response.data.city}`);
      }
      // Fetch average rating
      try {
        const ratingRes = await api.get(`/api/ratings/user/${response.data.id}/average`);
        setAverageRating(ratingRes.data);
      } catch (e) {
        console.error("Failed to fetch average rating", e);
      }
    } catch (err) {
      console.error("Fetch profile error:", err);
    }
  };

  const fetchListings = async () => {
    try {
      const response = await api.get('/api/donor/listings/my');
      setListings(response.data);
    } catch (err) {
      console.error("Fetch listings error:", err);
    }
  };

  const fetchUnreadCounts = async () => {
    try {
      const res = await api.get('/api/chat/unread-count');
      setUnreadCounts(res.data.byListing || {});
    } catch (err) {
      console.error(err);
    }
  };

  const handleUploadDoc = async () => {
    if (!docFile) return;
    setUploadingDoc(true);
    try {
      const formData = new FormData();
      formData.append('document', docFile);
      await api.post('/api/donor/verify/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success("Document uploaded for verification!");
      setDocFile(null);
      fetchProfile(); // Refresh profile to show PENDING status
    } catch (err) {
      toast.error(err.response?.data?.error || "Upload failed");
    } finally {
      setUploadingDoc(false);
    }
  };

  const handleRatingSubmit = async () => {
    if (ratingScore < 1 || ratingScore > 5) return;
    try {
      await api.post('/api/ratings', {
        listingId: ratingListing.id,
        score: ratingScore,
        comment: ratingComment
      });
      toast.success('Rating submitted successfully!');
      setRatingListing(null);
      setRatingScore(5);
      setRatingComment('');
    } catch (err) {
      toast.error(err.response?.data || 'Failed to submit rating');
    }
  };

  const handlePostFood = async (e) => {
    e.preventDefault();
    if (!foodName || !quantity || !location || !pickupMinutes) {
      toast.error(t('common.error'));
      return;
    }

    setSubmitLoading(true);
    try {
      const payload = {
        foodName,
        quantity,
        foodType,
        condition,
        location,
        pickupMinutes: parseInt(pickupMinutes),
        latitude: profile?.latitude || 13.0827,
        longitude: profile?.longitude || 80.2707
      };

      await api.post('/api/donor/listings', payload);
      toast.success("Food donation posted! Nearby NGOs notified.");
      
      // Reset form
      setFoodName('');
      setQuantity('');
      setPickupMinutes(60);
      setAiEstimated(false);
      
      // Refresh list
      fetchListings();
    } catch (err) {
      console.error(err);
      toast.error(t('common.error'));
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    toast.success("Logged out successfully");
    navigate('/login');
  };

  // Calculate stats
  const totalListings = listings.length;
  const claimedCount = listings.filter(l => l.status === 'CLAIMED').length;
  const activeCount = listings.filter(l => l.status === 'ACTIVE').length;

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h2>FoodBridge Donor Portal</h2>
          <p>
            {t('auth.welcome', 'Welcome back')}, <strong>{profile?.name}</strong>
            {averageRating !== null && averageRating > 0 && <span className="badge" style={{marginLeft: '10px', background: '#fef3c7', color: '#b45309', padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem'}}>⭐ {averageRating.toFixed(1)}</span>}
            {profile?.verificationStatus === 'VERIFIED' && <span className="badge" style={{marginLeft: '10px', background: '#dcfce7', color: '#166534', padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem'}}>✅ Verified</span>}
            {profile?.verificationStatus === 'PENDING' && <span className="badge" style={{marginLeft: '10px', background: '#fef9c3', color: '#854d0e', padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem'}}>⏳ Pending Verification</span>}
            {profile?.verificationStatus === 'REJECTED' && <span className="badge" style={{marginLeft: '10px', background: '#fee2e2', color: '#991b1b', padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem'}}>❌ Rejected</span>}
          </p>
          
          {profile && profile.verificationStatus !== 'VERIFIED' && profile.verificationStatus !== 'PENDING' && (
            <div className="verification-upload" style={{marginTop: '10px', fontSize: '0.9rem', display: 'flex', gap: '10px', alignItems: 'center', background: 'var(--card-bg)', padding: '10px', borderRadius: '8px', border: '1px solid var(--gray-light)'}}>
              <span style={{color: 'var(--text-color)'}}>{profile.verificationStatus === 'REJECTED' ? 'Re-upload document:' : 'Get verified (FSSAI/ID):'}</span>
              <input type="file" accept="image/jpeg, image/png, application/pdf" onChange={e => setDocFile(e.target.files[0])} style={{fontSize: '0.8rem', maxWidth: '200px'}} />
              {docFile && <button className="btn btn-primary btn-small" onClick={handleUploadDoc} disabled={uploadingDoc}>{uploadingDoc ? 'Uploading...' : 'Upload'}</button>}
            </div>
          )}
        </div>
        <div className="header-actions">
          <ThemeToggle />
          <Link to="/leaderboard" className="btn btn-secondary btn-small">🏆 {t('nav.impact')}</Link>
          <Link to="/certificate" className="btn btn-secondary btn-small">🌿 {t('impact.certTitle', 'Impact Certificate')}</Link>
          <button onClick={handleLogout} className="btn btn-danger btn-small">{t('auth.logout')}</button>
        </div>
      </header>

      {/* Main Grid */}
      <div className="dashboard-grid">
        {/* Left Form */}
        <div className="dashboard-card form-card">
          <h3>{t('dashboard.donor.title')}</h3>
          <p className="subtitle">{t('dashboard.donor.subtitle')}</p>
          <form onSubmit={handlePostFood} className="post-food-form">
            <div className="form-group">
              <label>{t('listing.foodName')}</label>
              <div className="input-with-button">
                <input
                  type="text"
                  placeholder="e.g. 15 Packets of Paneer Pulao"
                  value={foodName}
                  onChange={(e) => { setFoodName(e.target.value); setAiEstimated(false); }}
                  required
                />
                <button
                  type="button"
                  className="btn btn-secondary btn-camera"
                  onClick={() => setShowEstimator(true)}
                  title="Estimate details from photo using AI"
                >
                  📸
                </button>
              </div>
              {aiEstimated && (
                <span className="ai-badge">{t('donor.aiEstimate')}</span>
              )}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('listing.quantity')}</label>
                <input
                  type="text"
                  placeholder="e.g. 15 Packets / 10 Kgs"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>{t('listing.deadline')}</label>
                <input
                  type="number"
                  placeholder="60"
                  value={pickupMinutes}
                  onChange={(e) => setPickupMinutes(e.target.value)}
                  min="15"
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>{t('donor.foodType')}</label>
                <select value={foodType} onChange={(e) => setFoodType(e.target.value)}>
                  <option value="COOKED_MEAL">COOKED MEAL</option>
                  <option value="SNACKS">SNACKS</option>
                  <option value="RAW">RAW INGREDIENTS</option>
                </select>
              </div>
              <div className="form-group">
                <label>{t('donor.condition')}</label>
                <select value={condition} onChange={(e) => setCondition(e.target.value)}>
                  <option value="FRESH">FRESH</option>
                  <option value="GOOD">GOOD</option>
                  <option value="USE_SOON">USE SOON</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>{t('listing.location')}</label>
              <input
                type="text"
                placeholder="e.g. 45 Grand Palace Road, Guindy"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={submitLoading}>
              {submitLoading ? t('common.loading') : t('listing.postFood')}
            </button>
          </form>
        </div>

        {/* Right Stats & Listings */}
        <div className="dashboard-content-area">
          {/* Stats Cards */}
          <div className="stats-row">
            <div className="mini-stat-card">
              <h4>{totalListings}</h4>
              <p>{t('dashboard.donor.myListings')}</p>
            </div>
            <div className="mini-stat-card">
              <h4>{claimedCount}</h4>
              <p>{t('listing.claimed')}</p>
            </div>
            <div className="mini-stat-card">
              <h4>{activeCount}</h4>
              <p>{t('auth.status')}</p>
            </div>
            <div className="mini-stat-card">
              <h4>{(claimedCount * 12) || 0}</h4>
              <p>{t('impact.mealsRescued')}</p>
            </div>
          </div>

          {/* Listings Table */}
          <div className="dashboard-card table-card">
            <h3>{t('dashboard.donor.myListings')}</h3>
            {listings.length === 0 ? (
              <p className="no-data">No food listings posted yet.</p>
            ) : (
              <div className="table-responsive">
                <table>
                  <thead>
                    <tr>
                      <th>{t('listing.foodName')}</th>
                      <th>{t('listing.quantity')}</th>
                      <th>{t('donor.foodType')}</th>
                      <th>{t('listing.location')}</th>
                      <th>{t('auth.status')}</th>
                      <th>{t('donor.qrModalTitle', 'QR Verification')}</th>
                      <th>Chat</th>
                    </tr>
                  </thead>
                  <tbody>
                    {listings.map((item) => (
                      <tr key={item.id}>
                        <td><strong>{item.foodName}</strong></td>
                        <td>{item.quantity}</td>
                        <td><span className="type-badge">{item.foodType}</span></td>
                        <td>{item.location}</td>
                        <td>
                          <span className={`status-badge status-${item.status.toLowerCase()}`}>
                            {item.status === 'CLAIMED' ? t('listing.claimed') : item.status}
                          </span>
                        </td>
                        <td>
                          {item.qrCode ? (
                            <button
                              onClick={() => setViewingQR(item.qrCode)}
                              className="btn btn-secondary btn-small"
                            >
                              {t('donor.qrModalTitle', 'Show QR Code')}
                            </button>
                          ) : (
                            <span className="no-qr">N/A</span>
                          )}
                        </td>
                        <td>
                          {item.status === 'CLAIMED' ? (
                            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                              <button
                                onClick={() => setActiveChatListing(item)}
                                className="btn btn-primary btn-small chat-btn"
                              >
                                💬 Chat
                                {unreadCounts[item.id] > 0 && (
                                  <span className="unread-badge">{unreadCounts[item.id]}</span>
                                )}
                              </button>
                              <button 
                                onClick={() => setRatingListing(item)}
                                className="btn btn-secondary btn-small"
                                style={{ background: '#fef3c7', color: '#b45309', borderColor: '#fef3c7' }}
                              >
                                ⭐ Rate this pickup
                              </button>
                            </div>
                          ) : (
                            <span className="no-qr" style={{ color: 'var(--gray-dark)', fontSize: '0.85rem' }}>Available after claim</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Rating Modal */}
      {ratingListing && (
        <div className="modal-overlay" onClick={() => setRatingListing(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Rate the NGO Pickup</h3>
            <p>How was your experience for <strong>{ratingListing.foodName}</strong>?</p>
            
            <div className="form-group" style={{ marginTop: '20px' }}>
              <label>Score (1-5)</label>
              <select value={ratingScore} onChange={(e) => setRatingScore(parseInt(e.target.value))}>
                <option value={5}>⭐⭐⭐⭐⭐ (5 - Excellent)</option>
                <option value={4}>⭐⭐⭐⭐ (4 - Good)</option>
                <option value={3}>⭐⭐⭐ (3 - Average)</option>
                <option value={2}>⭐⭐ (2 - Poor)</option>
                <option value={1}>⭐ (1 - Terrible)</option>
              </select>
            </div>
            
            <div className="form-group">
              <label>Comment (Optional)</label>
              <textarea 
                value={ratingComment} 
                onChange={(e) => setRatingComment(e.target.value)}
                placeholder="Share your experience..."
                rows="3"
                style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid var(--gray-light)' }}
              />
            </div>
            
            <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
              <button onClick={handleRatingSubmit} className="btn btn-primary" style={{ flex: '1' }}>Submit Rating</button>
              <button onClick={() => setRatingListing(null)} className="btn btn-secondary" style={{ flex: '1' }}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* QR Code Modal */}
      {viewingQR && (
        <div className="modal-overlay" onClick={() => setViewingQR(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>{t('donor.qrModalTitle')}</h3>
            <p>{t('donor.qrModalSub')}</p>
            <div className="qr-container">
              <img src={viewingQR} alt="Verification QR Code" />
            </div>
            <button onClick={() => setViewingQR(null)} className="btn btn-primary">{t('donor.close')}</button>
          </div>
        </div>
      )}

      {/* Photo Estimator Modal */}
      {showEstimator && (
        <PhotoEstimator
          onConfirm={(est) => {
            setFoodName(est.foodName);
            setQuantity(est.quantity + " Portions");
            setCondition(est.condition);
            setFoodType(est.foodType);
            setAiEstimated(true);
          }}
          onClose={() => setShowEstimator(false)}
        />
      )}

      {/* Rescue Chat Modal */}
      {activeChatListing && (
        <RescueChat
          listingId={activeChatListing.id}
          listingTitle={activeChatListing.foodName}
          currentUser={profile}
          onClose={() => setActiveChatListing(null)}
          onMessageRead={(id) => setUnreadCounts(prev => ({ ...prev, [id]: 0 }))}
        />
      )}
    </div>
  );
};

export default DonorDashboard;
