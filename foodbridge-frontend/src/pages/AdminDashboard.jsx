import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import ThemeToggle from '../components/ThemeToggle';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const AdminDashboard = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState({
    totalMeals: 0,
    totalNgos: 0,
    totalDonors: 0,
    totalListings: 0,
    liveListings: 0
  });
  const [users, setUsers] = useState([]);
  const [listings, setListings] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingListings, setLoadingListings] = useState(true);
  const [loadingAnalytics, setLoadingAnalytics] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchStats();
    fetchUsers();
    fetchListings();
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    setLoadingAnalytics(true);
    try {
      const response = await api.get('/api/admin/analytics');
      setAnalytics(response.data);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load analytics');
    } finally {
      setLoadingAnalytics(false);
    }
  };

  const fetchStats = async () => {
    try {
      const response = await api.get('/api/admin/stats');
      setStats(response.data);
    } catch (err) {
      toast.error(t('common.error'));
    }
  };

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      const response = await api.get('/api/admin/users');
      setUsers(response.data);
    } catch (err) {
      toast.error(t('common.error'));
    } finally {
      setLoadingUsers(false);
    }
  };

  const fetchListings = async () => {
    setLoadingListings(true);
    try {
      const response = await api.get('/api/admin/listings');
      setListings(response.data);
    } catch (err) {
      toast.error(t('common.error'));
    } finally {
      setLoadingListings(false);
    }
  };

  const handleStatusChange = async (userId, newStatus) => {
    try {
      const response = await api.put(`/api/admin/users/${userId}/status?status=${newStatus}`);
      toast.success(`User status updated to ${newStatus}`);
      
      // Update local state
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, status: newStatus } : u));
      
      // Refresh stats
      fetchStats();
    } catch (err) {
      console.error(err);
      toast.error(t('common.error'));
    }
  };

  const handleVerificationChange = async (userId, newStatus) => {
    try {
      await api.put(`/api/admin/users/${userId}/verify?status=${newStatus}`);
      toast.success(`Verification status updated to ${newStatus}`);
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, verificationStatus: newStatus } : u));
    } catch (err) {
      console.error(err);
      toast.error(t('common.error'));
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
          <h2>{t('dashboard.admin.title')}</h2>
          <p>{t('dashboard.admin.subtitle')}</p>
        </div>
        <div className="header-actions">
          <ThemeToggle />
          <button onClick={handleLogout} className="btn btn-danger">{t('auth.logout')}</button>
        </div>
      </header>

      {/* Stats Cards */}
      <div className="stats-row">
        <div className="mini-stat-card admin-stat">
          <h4>{stats.totalMeals}</h4>
          <p>{t('admin.mealsDistributed', 'Total Meals Distributed')}</p>
        </div>
        <div className="mini-stat-card admin-stat">
          <h4>{stats.totalNgos}</h4>
          <p>{t('admin.regNgos', 'Registered NGOs')}</p>
        </div>
        <div className="mini-stat-card admin-stat">
          <h4>{stats.totalDonors}</h4>
          <p>{t('admin.actDonors', 'Active Donors')}</p>
        </div>
        <div className="mini-stat-card admin-stat">
          <h4>{stats.liveListings} / {stats.totalListings}</h4>
          <p>{t('admin.actListings', 'Active / Total Listings')}</p>
        </div>
      </div>

      <div className="admin-double-panel">
        {/* User Moderation */}
        <div className="dashboard-card table-card">
          <div className="panel-header">
            <h3>{t('admin.userMod', 'User Access Moderation')}</h3>
            <button onClick={fetchUsers} className="btn btn-secondary btn-small">{t('admin.reload', 'Reload')}</button>
          </div>
          {loadingUsers ? (
            <p className="no-data">{t('common.loading')}</p>
          ) : users.length === 0 ? (
            <p className="no-data">{t('admin.noUsers', 'No registered users on the platform.')}</p>
          ) : (
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>{t('auth.name')}</th>
                    <th>{t('auth.email')}</th>
                    <th>{t('auth.role')}</th>
                    <th>{t('auth.city')}</th>
                    <th>{t('auth.status')}</th>
                    <th>Verification</th>
                    <th>{t('admin.actions', 'Actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td><strong>{u.name}</strong></td>
                      <td>{u.email}</td>
                      <td><span className="role-tag">{u.role}</span></td>
                      <td>{u.city}</td>
                      <td>
                        <span className={`status-badge status-${u.status.toLowerCase()}`}>
                          {u.status}
                        </span>
                      </td>
                      <td>
                        {u.role === 'DONOR' ? (
                          <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
                            <span className={`status-badge`} style={{
                              background: u.verificationStatus === 'VERIFIED' ? '#dcfce7' : u.verificationStatus === 'REJECTED' ? '#fee2e2' : '#fef9c3',
                              color: u.verificationStatus === 'VERIFIED' ? '#166534' : u.verificationStatus === 'REJECTED' ? '#991b1b' : '#854d0e',
                              fontSize: '0.75rem',
                              padding: '2px 8px'
                            }}>
                              {u.verificationStatus || 'PENDING'}
                            </span>
                            {u.verificationDocumentUrl && (
                              <a href={u.verificationDocumentUrl} target="_blank" rel="noopener noreferrer" style={{fontSize: '0.75rem', color: 'var(--primary)'}}>View Doc</a>
                            )}
                          </div>
                        ) : '-'}
                      </td>
                      <td>
                        <div className="action-buttons-cell" style={{flexWrap: 'wrap', gap: '8px', minWidth: '150px'}}>
                          {u.status !== 'ACTIVE' && (
                            <button
                              onClick={() => handleStatusChange(u.id, 'ACTIVE')}
                              className="btn btn-primary btn-small"
                            >
                              {t('admin.approve', 'Approve')}
                            </button>
                          )}
                          {u.status !== 'BLOCKED' && (
                            <button
                              onClick={() => handleStatusChange(u.id, 'BLOCKED')}
                              className="btn btn-danger btn-small"
                            >
                              {t('admin.block', 'Block')}
                            </button>
                          )}
                          {u.role === 'DONOR' && u.verificationStatus === 'PENDING' && u.verificationDocumentUrl && (
                            <>
                              <button onClick={() => handleVerificationChange(u.id, 'VERIFIED')} className="btn btn-primary btn-small" style={{background: '#166534', borderColor: '#166534'}}>Verify</button>
                              <button onClick={() => handleVerificationChange(u.id, 'REJECTED')} className="btn btn-danger btn-small">Reject Doc</button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Global Listing Tracker */}
        <div className="dashboard-card table-card">
          <div className="panel-header">
            <h3>{t('admin.globalTracker', 'Global Food Listings Tracker')}</h3>
            <button onClick={fetchListings} className="btn btn-secondary btn-small">{t('admin.reload', 'Reload')}</button>
          </div>
          {loadingListings ? (
            <p className="no-data">{t('common.loading')}</p>
          ) : listings.length === 0 ? (
            <p className="no-data">{t('admin.noListings', 'No food listings found.')}</p>
          ) : (
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>{t('listing.foodName')}</th>
                    <th>{t('listing.quantity')}</th>
                    <th>{t('listing.location')}</th>
                    <th>{t('auth.status')}</th>
                  </tr>
                </thead>
                <tbody>
                  {listings.map((l) => (
                    <tr key={l.id}>
                      <td>#{l.id}</td>
                      <td><strong>{l.foodName}</strong></td>
                      <td>{l.quantity}</td>
                      <td>{l.location}</td>
                      <td>
                        <span className={`status-badge status-${l.status.toLowerCase()}`}>
                          {l.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Platform Analytics Section */}
      <div className="dashboard-card form-card" style={{ marginTop: '30px' }}>
          <div className="panel-header">
            <h3>Platform Analytics</h3>
            <button onClick={fetchAnalytics} className="btn btn-secondary btn-small">Reload</button>
          </div>

          {loadingAnalytics ? (
            <p className="no-data">{t('common.loading', 'Loading...')}</p>
          ) : !analytics ? (
            <p className="no-data">Failed to load analytics data.</p>
          ) : (
            <div style={{ padding: '20px 0' }}>
              <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', marginBottom: '30px' }}>
                <div className="impact-box" style={{ flex: '1', minWidth: '200px', display: 'flex', alignItems: 'center', gap: '15px', padding: '20px', background: 'var(--surface-color)', borderRadius: '12px', border: '1px solid var(--gray-light)' }}>
                  <span className="box-icon" style={{ fontSize: '2rem' }}>📈</span>
                  <div className="box-details">
                    <h3 style={{ margin: 0, fontSize: '1.8rem', color: 'var(--primary)' }}>{analytics.claimRatePercentage.toFixed(1)}%</h3>
                    <p style={{ margin: '5px 0 0', color: 'var(--gray-dark)' }}>Global Claim Rate</p>
                  </div>
                </div>
                <div className="impact-box" style={{ flex: '1', minWidth: '200px', display: 'flex', alignItems: 'center', gap: '15px', padding: '20px', background: 'var(--surface-color)', borderRadius: '12px', border: '1px solid var(--gray-light)' }}>
                  <span className="box-icon" style={{ fontSize: '2rem' }}>⏱️</span>
                  <div className="box-details">
                    <h3 style={{ margin: 0, fontSize: '1.8rem', color: 'var(--primary)' }}>{analytics.averageTimeToClaimMinutes.toFixed(0)} min</h3>
                    <p style={{ margin: '5px 0 0', color: 'var(--gray-dark)' }}>Average Time-to-Claim</p>
                  </div>
                </div>
              </div>

              <div style={{ height: '350px', width: '100%' }}>
                <Line
                  data={{
                    labels: Object.keys(analytics.listingsPerDay),
                    datasets: [
                      {
                        label: 'Listings Created',
                        data: Object.values(analytics.listingsPerDay),
                        borderColor: 'rgb(29, 158, 117)',
                        backgroundColor: 'rgba(29, 158, 117, 0.5)',
                        tension: 0.3,
                        fill: true
                      }
                    ]
                  }}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                      legend: { position: 'top' },
                      title: { display: true, text: 'Listings Created (Last 30 Days)' }
                    },
                    scales: {
                      y: { beginAtZero: true, ticks: { stepSize: 1 } }
                    }
                  }}
                />
              </div>
            </div>
          )}
      </div>
    </div>
  );
};

export default AdminDashboard;
