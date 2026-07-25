import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import ThemeToggle from '../components/ThemeToggle';

const Leaderboard = () => {
  const { t } = useTranslation();
  const [donors, setDonors] = useState([]);
  const [ngos, setNgos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLeaderboards();
  }, []);

  const fetchLeaderboards = async () => {
    setLoading(true);
    try {
      const [donorsRes, ngosRes] = await Promise.all([
        api.get('/api/leaderboard/donors'),
        api.get('/api/leaderboard/ngos')
      ]);
      setDonors(donorsRes.data);
      setNgos(ngosRes.data);
    } catch (err) {
      toast.error(t('common.error'));
    } finally {
      setLoading(false);
    }
  };

  const getRankStyle = (rank) => {
    if (rank === 1) return { backgroundColor: '#ffd700', color: '#000', fontWeight: 'bold' }; // Gold
    if (rank === 2) return { backgroundColor: '#c0c0c0', color: '#000', fontWeight: 'bold' }; // Silver
    if (rank === 3) return { backgroundColor: '#cd7f32', color: '#fff', fontWeight: 'bold' }; // Bronze
    return { backgroundColor: '#e2e8f0', color: '#334155' };
  };

  return (
    <div className="leaderboard-page-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-brand">
          <h2>{t('impact.leaderboardTitle', 'FoodBridge Community Leaderboard')}</h2>
          <p>{t('impact.leaderboardSub', 'Recognizing our neighborhood heroes this month')}</p>
        </div>
        <div className="header-actions">
          <ThemeToggle />
          <Link to="/" className="btn btn-secondary btn-small">{t('nav.howItWorks', 'Home')}</Link>
          {localStorage.getItem('token') ? (
            <button
              onClick={() => {
                const role = localStorage.getItem('role');
                window.location.href = role === 'DONOR' ? '/donor/dashboard' : '/ngo/dashboard';
              }}
              className="btn btn-primary btn-small"
            >
              {t('nav.impact', 'Go to Dashboard')}
            </button>
          ) : (
            <Link to="/login" className="btn btn-primary btn-small">{t('nav.signIn')}</Link>
          )}
        </div>
      </header>

      {loading ? (
        <div className="no-data">{t('common.loading')}</div>
      ) : (
        <div className="leaderboard-grid-cols">
          {/* Top Donors Column */}
          <div className="leaderboard-col card-column">
            <div className="col-header">
              <h3>🏆 {t('impact.topDonors', 'Top Food Donors')}</h3>
              <p>{t('impact.topDonorsSub', 'Ranked by cumulative meals donated this month')}</p>
            </div>
            {donors.length === 0 ? (
              <p className="no-data">{t('impact.noDonors', 'No donor logs recorded yet this month.')}</p>
            ) : (
              <div className="leaderboard-list">
                {donors.map((d) => (
                  <div key={d.userId} className="leaderboard-row">
                    <div className="rank-badge" style={getRankStyle(d.rank)}>
                      {d.rank}
                    </div>
                    <div className="user-info">
                      <h4>{d.name}</h4>
                      <p className="city-label">{d.city}</p>
                    </div>
                    <div className="badge-info">
                      {d.topBadge !== 'None' && (
                        <span className="badge-tag">{d.topBadge}</span>
                      )}
                    </div>
                    <div className="score-info donor-score">
                      <strong>{d.mealsCount}</strong>
                      <span>{t('impact.mealsLabel', 'meals')}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Top NGOs Column */}
          <div className="leaderboard-col card-column">
            <div className="col-header">
              <h3>🛡️ {t('impact.topNgos', 'Top NGO Rescuers')}</h3>
              <p>{t('impact.topNgosSub', 'Ranked by number of completed food rescues this month')}</p>
            </div>
            {ngos.length === 0 ? (
              <p className="no-data">{t('impact.noNgos', 'No NGO rescues logged yet this month.')}</p>
            ) : (
              <div className="leaderboard-list">
                {ngos.map((n) => (
                  <div key={n.userId} className="leaderboard-row">
                    <div className="rank-badge" style={getRankStyle(n.rank)}>
                      {n.rank}
                    </div>
                    <div className="user-info">
                      <h4>{n.name}</h4>
                      <p className="city-label">{n.city}</p>
                    </div>
                    <div className="badge-info">
                      {n.topBadge !== 'None' && (
                        <span className="badge-tag">{n.topBadge}</span>
                      )}
                    </div>
                    <div className="score-info ngo-score">
                      <strong>{n.rescuesCount}</strong>
                      <span>{t('impact.rescuesLabel', 'rescues')}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Leaderboard;
