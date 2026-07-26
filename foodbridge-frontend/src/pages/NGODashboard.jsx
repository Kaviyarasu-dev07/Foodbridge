import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useTranslation } from 'react-i18next';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import api from '../services/api';
import CountdownTimer from '../components/CountdownTimer';
import ThemeToggle from '../components/ThemeToggle';
import RescueChat from '../components/RescueChat';
import RouteMap from '../components/RouteMap';
import PredictiveAlertBanner from '../components/PredictiveAlertBanner';

import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Fix Leaflet Default Icon issue in React/Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});

const NGODashboard = () => {
  const { t } = useTranslation();
  const [profile, setProfile] = useState(null);
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [wsConnected, setWsConnected] = useState(false);
  const [pushEnabled, setPushEnabled] = useState(false);
  const [activeChatListing, setActiveChatListing] = useState(null);
  const [unreadCounts, setUnreadCounts] = useState({});
  const [selectedListingIds, setSelectedListingIds] = useState([]);
  const [averageRating, setAverageRating] = useState(null);
  const [ratingListing, setRatingListing] = useState(null);
  const [ratingScore, setRatingScore] = useState(5);
  const [ratingComment, setRatingComment] = useState('');
  const [optimizedRoute, setOptimizedRoute] = useState(null);
  const [routeLoading, setRouteLoading] = useState(false);
  const [predictiveAlerts, setPredictiveAlerts] = useState([]);
  const stompClientRef = useRef(null);
  const navigate = useNavigate();

  const handlePlanRoute = async () => {
    setRouteLoading(true);
    try {
      const response = await api.post('/api/ngo/route/optimize', { listingIds: selectedListingIds });
      setOptimizedRoute(response.data);
      toast.success("Pickup route optimized successfully!");
    } catch (err) {
      console.error(err);
      toast.error("Failed to optimize route.");
    } finally {
      setRouteLoading(false);
    }
  };

  const fetchPredictiveAlerts = async () => {
    try {
      const res = await api.get('/api/ngo/alerts/predictive');
      setPredictiveAlerts(res.data || []);
    } catch (err) {
      console.error("Failed to load predictive alerts", err);
    }
  };

  useEffect(() => {
    fetchProfileAndData();
    fetchPredictiveAlerts();
    checkNotificationPermission();
    fetchUnreadCounts();
    const interval = setInterval(fetchUnreadCounts, 5000);
    return () => {
      clearInterval(interval);
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        console.log("WebSocket disconnected.");
      }
    };
  }, []);

  const fetchUnreadCounts = async () => {
    try {
      const res = await api.get('/api/chat/unread-count');
      setUnreadCounts(res.data.byListing || {});
    } catch (err) {
      console.error(err);
    }
  };

  const checkNotificationPermission = async () => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      console.log('Push messaging is not supported in this browser.');
      return;
    }
    if (Notification.permission === 'granted') {
      setPushEnabled(true);
      subscribeToPush(true);
    }
  };

  const subscribeToPush = async (silent = false) => {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      if (!silent) toast.error("Push messaging is not supported in this browser.");
      return;
    }

    try {
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        if (!silent) toast.error("Notification permission denied.");
        return;
      }

      // Get VAPID public key
      const keyResp = await api.get('/api/public/push/vapid-public-key');
      const base64PublicKey = keyResp.data.publicKey;
      
      const registration = await navigator.serviceWorker.ready;
      let subscription = await registration.pushManager.getSubscription();

      if (!subscription) {
        subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: base64PublicKey
        });
      }

      const subJson = subscription.toJSON();
      await api.post('/api/push/subscribe', {
        endpoint: subJson.endpoint,
        p256dh: subJson.keys?.p256dh,
        auth: subJson.keys?.auth
      });

      setPushEnabled(true);
      if (!silent) toast.success("Successfully subscribed to real-time Web Push notifications!");
    } catch (err) {
      console.error("Failed to subscribe to push notifications", err);
      if (!silent) toast.error("Failed to subscribe to push notifications.");
    }
  };

  const fetchProfileAndData = async () => {
    try {
      const response = await api.get('/api/auth/me');
      setProfile(response.data);
      
      try {
        const ratingRes = await api.get(`/api/ratings/user/${response.data.id}/average`);
        setAverageRating(ratingRes.data);
      } catch (e) {
        console.error("Failed to fetch average rating", e);
      }

      // Fetch nearby listings using profile coordinates
      fetchNearby(response.data.latitude || 13.0827, response.data.longitude || 80.2707);

      // Connect WebSockets
      connectWebSocket();
    } catch (err) {
      toast.error("Failed to load NGO profile");
      setLoading(false);
    }
  };

  const fetchNearby = async (lat, lng) => {
    setLoading(true);
    try {
      const response = await api.get(`/api/ngo/listings/nearby?lat=${lat}&lng=${lng}`);
      setListings(response.data);
    } catch (err) {
      toast.error("Failed to load nearby listings");
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    try {
      const socket = new SockJS('/ws');
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.onConnect = (frame) => {
        setWsConnected(true);
        console.log("WebSocket Connected!");

        // Subscribe to nearby food alerts
        client.subscribe('/topic/food-alerts', (message) => {
          const alert = JSON.parse(message.body);
          // Show hot toast
          toast((t) => (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <span>🍲 <strong>New Food Alert!</strong></span>
              <span>{alert.foodName} ({alert.quantity}) is available at {alert.location}</span>
              <button
                onClick={() => {
                  toast.dismiss(t.id);
                  if (profile) fetchNearby(profile.latitude, profile.longitude);
                }}
                style={{
                  background: '#1D9E75',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  cursor: 'pointer',
                  fontSize: '12px',
                  marginTop: '4px',
                  alignSelf: 'flex-start'
                }}
              >
                Refresh List
              </button>
            </div>
          ), { duration: 8000 });

          // Refresh the listings
          if (profile) fetchNearby(profile.latitude, profile.longitude);
        });

        // Subscribe to SOS broadcasts
        client.subscribe('/topic/food-sos', (message) => {
          const alert = JSON.parse(message.body);
          toast.error(
            `🚨 EMERGENCY SOS BROADCAST: Urgent food rescue needed for ${alert.foodName} (${alert.quantity}) at ${alert.location}!`,
            { duration: 10000, position: 'top-center' }
          );

          if (profile) fetchNearby(profile.latitude, profile.longitude);
        });

        // Subscribe to predictive alerts
        client.subscribe('/topic/predictive-alerts', (message) => {
          const alert = JSON.parse(message.body);
          toast.success(`🔮 AI Prediction: ${alert.message}`, { duration: 8000 });
          setPredictiveAlerts(prev => [alert, ...prev]);
        });
      };

      client.onDisconnect = () => {
        setWsConnected(false);
        console.log("WebSocket Disconnected.");
      };

      client.onStompError = (frame) => {
        console.error("STOMP error", frame);
      };

      client.activate();
      stompClientRef.current = client;
    } catch (e) {
      console.error("WebSocket setup failed", e);
    }
  };

  const handleClaim = async (id) => {
    try {
      const response = await api.put(`/api/ngo/listings/${id}/claim`);
      toast.success("Food claimed successfully! Check your dashboard for routing details.");
      
      // Update local state listing status to CLAIMED
      setListings(prev => prev.map(item => item.id === id ? { ...item, status: 'CLAIMED' } : item));
      
      // Open verification modal or show details
      if (response.data.listing?.qrCode) {
        toast.success("Checkout QR code available for scanning.");
      }
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || "Failed to claim listing.");
    }
  };

  const triggerSOS = async (id) => {
    try {
      await api.post(`/api/ngo/listings/${id}/sos`);
      toast.success("SOS citywide broadcast triggered successfully!");
    } catch (err) {
      toast.error("Failed to trigger SOS alert.");
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

  const handleLogout = () => {
    localStorage.clear();
    toast.success("Logged out successfully");
    navigate('/login');
  };

  const ngoCenter = [profile?.latitude || 13.0827, profile?.longitude || 80.2707];

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h2>{t('ngo.portalTitle', 'FoodBridge NGO Portal')}</h2>
          <p>
            {t('auth.welcome', 'Welcome')}, <strong>{profile?.name}</strong> | {t('auth.status')}:{' '}
            <span className={`status-badge status-${profile?.status.toLowerCase()}`}>
              {profile?.status}
            </span>
            {averageRating !== null && averageRating > 0 && <span className="badge" style={{marginLeft: '10px', background: '#fef3c7', color: '#b45309', padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem'}}>⭐ {averageRating.toFixed(1)}</span>}
          </p>
        </div>
        <div className="header-actions">
          <div className="ws-status">
            <span className={`dot ${wsConnected ? 'connected' : 'disconnected'}`}></span>
            {wsConnected ? t('impact.liveFeed', 'Live Connection Active') : 'Connecting to Server...'}
          </div>
          <ThemeToggle />
          {pushEnabled ? (
            <span className="push-badge">{t('ngo.notifEnabled', '🟢 Notifications enabled')}</span>
          ) : (
            <button onClick={() => subscribeToPush(false)} className="btn btn-secondary btn-small">
              {t('ngo.enableNotif', '🔔 Enable notifications')}
            </button>
          )}
          <Link to="/leaderboard" className="btn btn-secondary btn-small">🏆 {t('nav.impact')}</Link>
          <Link to="/certificate" className="btn btn-secondary btn-small">🌿 {t('impact.certTitle', 'Impact Certificate')}</Link>
          <button onClick={handleLogout} className="btn btn-danger btn-small">{t('auth.logout')}</button>
        </div>
      </header>

      {/* Grid Layout: Left listings list, Right Map */}
      {predictiveAlerts.length > 0 && (
        <PredictiveAlertBanner alert={predictiveAlerts[0]} />
      )}
      <div className="ngo-dashboard-layout">
        {/* Listings Section */}
        <div className="ngo-listings-panel">
          <div className="panel-header">
            <h3>{t('dashboard.ngo.title')}</h3>
            <button
              onClick={() => profile && fetchNearby(profile.latitude, profile.longitude)}
              className="btn btn-secondary btn-small"
            >
              {t('ngo.refreshFeed', 'Refresh Feed')}
            </button>
          </div>

          {predictiveAlerts.length > 0 && (
            <div className="predictive-alerts-container">
              {predictiveAlerts.map((alert, idx) => {
                const { donorName, donorLocation, commonFoodType, averageQuantity, hourOfDay, confidenceScore } = alert.pattern;
                const formatHour = (h) => {
                  const period = h >= 12 ? 'PM' : 'AM';
                  const hr = h % 12 || 12;
                  return `${hr}:00 ${period}`;
                };
                return (
                  <div key={idx} className="predictive-alert-card">
                    <div className="predictive-badge-row">
                      <span className="predictive-tag">🔮 AI Surplus Prediction</span>
                      <span className="confidence-badge">{confidenceScore}% Confidence</span>
                    </div>
                    <h4>{donorName}</h4>
                    <p>📍 {donorLocation} ({alert.distanceKm} km away)</p>
                    <p>📦 Expected: <strong>{averageQuantity} packets of {commonFoodType?.replace('_', ' ')}</strong></p>
                    <p className="expected-time">⏰ Usually posts around {formatHour(hourOfDay)}</p>
                    <button
                      onClick={() => setPredictiveAlerts(prev => prev.filter((_, i) => i !== idx))}
                      className="btn-dismiss"
                    >
                      ✕ Dismiss alert
                    </button>
                  </div>
                );
              })}
            </div>
          )}

          {selectedListingIds.length >= 2 && (
            <div className="plan-route-banner">
              <span>📍 {selectedListingIds.length} locations selected for pickup</span>
              <button
                onClick={handlePlanRoute}
                disabled={routeLoading}
                className="btn btn-primary"
              >
                {routeLoading ? 'Calculating...' : 'Plan pickup route'}
              </button>
            </div>
          )}

          {loading ? (
            <p className="no-data">{t('common.loading')}</p>
          ) : listings.length === 0 ? (
            <p className="no-data">{t('dashboard.ngo.noFood')}</p>
          ) : (
            <div className="listings-scroll-list">
              {listings.map((item) => (
                <div key={item.id} className={`ngo-listing-card ${item.status === 'CLAIMED' ? 'claimed-card' : ''}`}>
                  <div className="listing-checkbox-wrapper" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', borderBottom: '1px solid var(--gray-light)', paddingBottom: '8px' }}>
                    <input
                      type="checkbox"
                      id={`select-${item.id}`}
                      checked={selectedListingIds.includes(item.id)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedListingIds(prev => [...prev, item.id]);
                        } else {
                          setSelectedListingIds(prev => prev.filter(id => id !== item.id));
                        }
                      }}
                    />
                    <label htmlFor={`select-${item.id}`} style={{ fontSize: '0.85rem', fontWeight: '600', color: 'var(--dark)', cursor: 'pointer' }}>
                      Select for Route Pickup
                    </label>
                  </div>
                  <div className="card-header-row">
                    <span className="type-badge">{item.foodType}</span>
                    <span className={`condition-tag cond-${item.condition.toLowerCase()}`}>
                      {item.condition}
                    </span>
                  </div>
                  <h4>{item.foodName}</h4>
                  <p className="quantity"><strong>{t('listing.quantity')}:</strong> {item.quantity}</p>
                  <p className="detail"><strong>{t('listing.location')}:</strong> {item.location}</p>
                  <p className="detail"><strong>Donor:</strong> {item.donor?.name || 'Local Donor'}</p>
                  
                  <div className="timer-row">
                    <strong>{t('listing.minutesLeft', 'Time Left')}: </strong>
                    <CountdownTimer expiresAt={item.expiresAt} />
                  </div>

                  <div className="card-actions">
                    {item.status === 'ACTIVE' ? (
                      <>
                        <button
                          onClick={() => handleClaim(item.id)}
                          className="btn btn-primary btn-block"
                        >
                          {t('listing.claimNow')}
                        </button>
                        <button
                          onClick={() => triggerSOS(item.id)}
                          className="btn btn-warning"
                          title="Trigger citywide emergency broadcast alert"
                        >
                          {t('ngo.sosButton', '🚨 SOS')}
                        </button>
                      </>
                    ) : (
                      <div className="claimed-status-overlay" style={{ display: 'flex', flexDirection: 'column', gap: '10px', width: '100%' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <span>✅ {t('ngo.claimedByYou', 'Claimed by You')}</span>
                          <button
                            onClick={() => setActiveChatListing(item)}
                            className="btn btn-primary btn-small chat-btn"
                          >
                            💬 Chat
                            {unreadCounts[item.id] > 0 && (
                              <span className="unread-badge">{unreadCounts[item.id]}</span>
                            )}
                          </button>
                        </div>
                        <button 
                          onClick={() => setRatingListing(item)}
                          className="btn btn-secondary btn-small"
                          style={{ background: '#fef3c7', color: '#b45309', borderColor: '#fef3c7', width: '100%' }}
                        >
                          ⭐ Rate Donor
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Map Panel */}
        <div className="ngo-map-panel">
          <h3>{t('ngo.mapTitle', 'Interactive Food Rescue Map')}</h3>
          <div className="map-container-wrapper">
            <MapContainer center={ngoCenter} zoom={13} style={{ height: '100%', width: '100%' }}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              
              {/* NGO Marker */}
              {profile && (
                <Marker position={[profile.latitude, profile.longitude]}>
                  <Popup>
                    <strong>Your Location ({profile.name})</strong>
                  </Popup>
                </Marker>
              )}

              {/* Active Listings Markers */}
              {listings.filter(l => l.status === 'ACTIVE').map((item) => (
                <Marker key={item.id} position={[item.latitude, item.longitude]}>
                  <Popup>
                    <strong>{item.foodName}</strong><br/>
                    Qty: {item.quantity}<br/>
                    Location: {item.location}<br/>
                    <button
                      onClick={() => handleClaim(item.id)}
                      style={{
                        background: '#1D9E75',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        padding: '4px 8px',
                        cursor: 'pointer',
                        marginTop: '8px',
                        width: '100%'
                      }}
                    >
                      {t('listing.claimNow')}
                    </button>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        </div>
      </div>

      {/* Rating Modal */}
      {ratingListing && (
        <div className="modal-overlay" onClick={() => setRatingListing(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <h3>Rate the Donor</h3>
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

      {/* Route Map Modal */}
      {optimizedRoute && (
        <RouteMap
          route={optimizedRoute}
          onClose={() => setOptimizedRoute(null)}
        />
      )}
    </div>
  );
};

export default NGODashboard;
