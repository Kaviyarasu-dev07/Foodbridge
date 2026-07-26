import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import ThemeToggle from '../components/ThemeToggle';
import RouteMap from '../components/RouteMap';

const VolunteerDashboard = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [availableListings, setAvailableListings] = useState([]);
  const [acceptedListings, setAcceptedListings] = useState([]);
  const [activeTab, setActiveTab] = useState('available');
  const [loading, setLoading] = useState(true);
  
  const [routeData, setRouteData] = useState(null);
  const [optimizing, setOptimizing] = useState(false);

  useEffect(() => {
    fetchProfile();
    fetchData();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/api/auth/me');
      setProfile(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const [availRes, acceptRes] = await Promise.all([
        api.get('/api/volunteer/listings/available'),
        api.get('/api/volunteer/listings/accepted')
      ]);
      setAvailableListings(availRes.data);
      setAcceptedListings(acceptRes.data);
      
      // If we have accepted listings and we don't have a route yet, let's optimize automatically
      if (acceptRes.data.length > 0 && !routeData) {
        handleOptimizeRoute(acceptRes.data);
      } else if (acceptRes.data.length === 0) {
        setRouteData(null);
      }
    } catch (err) {
      toast.error(t('common.error', 'Something went wrong'));
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (id) => {
    try {
      await api.put(`/api/volunteer/listings/${id}/accept`);
      toast.success('Delivery accepted! View in My Route.');
      fetchData();
    } catch (err) {
      toast.error('Failed to accept delivery.');
    }
  };

  const handleComplete = async (id) => {
    try {
      await api.put(`/api/volunteer/listings/${id}/complete`);
      toast.success('Delivery marked as completed! Great job!');
      fetchData();
    } catch (err) {
      toast.error('Failed to complete delivery.');
    }
  };

  const handleOptimizeRoute = async (listingsToOptimize = acceptedListings) => {
    if (listingsToOptimize.length === 0) {
      toast.error('No accepted deliveries to route.');
      return;
    }
    
    setOptimizing(true);
    try {
      const payload = {
        listingIds: listingsToOptimize.map(l => l.id)
      };
      const response = await api.post('/api/volunteer/route/optimize', payload);
      setRouteData(response.data);
      toast.success('Route optimized!');
    } catch (err) {
      console.error("Optimization failed:", err);
      toast.error('Failed to generate optimized route.');
    } finally {
      setOptimizing(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    toast.success("Logged out successfully");
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h2>Volunteer Driver Portal</h2>
          <p>{t('auth.welcome', 'Welcome back')}, <strong>{profile?.name}</strong></p>
        </div>
        <div className="header-actions">
          <ThemeToggle />
          <button onClick={handleLogout} className="btn btn-danger btn-small">{t('auth.logout')}</button>
        </div>
      </header>

      {/* Main Grid */}
      <div className="dashboard-grid">
        
        {/* Left column (Tabs) */}
        <div className="dashboard-card form-card" style={{ flex: '1', minWidth: '300px' }}>
          <div className="tabs" style={{ display: 'flex', gap: '10px', marginBottom: '20px', borderBottom: '2px solid var(--gray-light)', paddingBottom: '10px' }}>
            <button 
              className={`btn ${activeTab === 'available' ? 'btn-primary' : 'btn-secondary'} btn-small`} 
              onClick={() => setActiveTab('available')}
              style={{ flex: 1 }}
            >
              Available ({availableListings.length})
            </button>
            <button 
              className={`btn ${activeTab === 'accepted' ? 'btn-primary' : 'btn-secondary'} btn-small`} 
              onClick={() => setActiveTab('accepted')}
              style={{ flex: 1 }}
            >
              My Route ({acceptedListings.length})
            </button>
          </div>

          {activeTab === 'available' && (
            <div className="listings-list">
              <h3>Available for Pickup</h3>
              <p className="subtitle">These foods have been claimed by an NGO and need transport.</p>
              
              {loading ? <p>Loading...</p> : availableListings.length === 0 ? (
                <p className="no-data">No deliveries available right now.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                  {availableListings.map(listing => (
                    <div key={listing.id} className="ngo-listing-card">
                      <div className="ngo-listing-header">
                        <h4>{listing.foodName}</h4>
                        <span className="badge pickup-badge">{listing.pickupMinutes} mins</span>
                      </div>
                      <p className="listing-detail"><strong>Quantity:</strong> {listing.quantity}</p>
                      <p className="listing-detail"><strong>Type:</strong> {listing.foodType}</p>
                      <p className="listing-detail"><strong>Location:</strong> {listing.location}</p>
                      <button onClick={() => handleAccept(listing.id)} className="btn btn-primary btn-block" style={{ marginTop: '10px' }}>
                        Accept Delivery
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeTab === 'accepted' && (
            <div className="listings-list">
              <h3>My Deliveries</h3>
              
              {loading ? <p>Loading...</p> : acceptedListings.length === 0 ? (
                <p className="no-data">You haven't accepted any deliveries yet.</p>
              ) : (
                <>
                  <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
                    <button onClick={() => handleOptimizeRoute()} className="btn btn-secondary btn-block" disabled={optimizing}>
                      {optimizing ? 'Calculating...' : 'Re-Optimize Route 🗺️'}
                    </button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {acceptedListings.map(listing => (
                      <div key={listing.id} className="ngo-listing-card" style={{ borderLeft: '4px solid var(--primary)' }}>
                        <div className="ngo-listing-header">
                          <h4>{listing.foodName}</h4>
                          <span className="status-badge status-claimed">IN PROGRESS</span>
                        </div>
                        <p className="listing-detail"><strong>Quantity:</strong> {listing.quantity}</p>
                        <p className="listing-detail"><strong>Location:</strong> {listing.location}</p>
                        <button onClick={() => handleComplete(listing.id)} className="btn btn-success btn-block" style={{ marginTop: '10px', background: '#166534', color: 'white' }}>
                          Mark as Completed ✅
                        </button>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        {/* Right column (Map/Route display) */}
        <div className="dashboard-content-area" style={{ flex: '2', minWidth: '400px' }}>
          {routeData ? (
            <div className="dashboard-card" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h3>Optimized Route</h3>
                <div style={{ display: 'flex', gap: '15px' }}>
                  <span className="badge" style={{ background: 'var(--primary)', color: 'white' }}>{routeData.totalDistanceKm} km</span>
                  <span className="badge" style={{ background: '#0284c7', color: 'white' }}>~{routeData.totalEstimatedMinutes} mins</span>
                </div>
              </div>
              
              <div style={{ flex: 1, minHeight: '400px', borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--gray-light)' }}>
                <RouteMap routeData={routeData} />
              </div>

              <div style={{ marginTop: '20px' }}>
                <h4>Stops Overview</h4>
                <ul className="route-stops-list">
                  {routeData.stops.map(stop => (
                    <li key={stop.stopOrder} className="route-stop-item">
                      <div className="stop-marker">{stop.stopOrder}</div>
                      <div className="stop-details">
                        <strong>{stop.foodName}</strong>
                        <span>{stop.location} • {stop.distanceFromPreviousKm} km</span>
                      </div>
                    </li>
                  ))}
                </ul>

                <a 
                  href={routeData.googleMapsUrl} 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-block"
                  style={{ marginTop: '20px', textAlign: 'center', display: 'block' }}
                >
                  Open in Google Maps 🚀
                </a>
              </div>
            </div>
          ) : (
            <div className="dashboard-card" style={{ height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#888' }}>
              <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🗺️</div>
              <h3>No Active Route</h3>
              <p>Accept deliveries to see your optimized travel path.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default VolunteerDashboard;
