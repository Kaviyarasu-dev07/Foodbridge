import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import ThemeToggle from '../components/ThemeToggle';

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
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingListings, setLoadingListings] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchStats();
    fetchUsers();
    fetchListings();
  }, []);

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
                        <div className="action-buttons-cell">
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
    </div>
  );
};

export default AdminDashboard;
