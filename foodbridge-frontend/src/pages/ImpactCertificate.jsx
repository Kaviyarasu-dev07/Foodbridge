import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import ThemeToggle from '../components/ThemeToggle';

const ImpactCertificate = () => {
  const { t } = useTranslation();
  const [impact, setImpact] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchImpactData();
  }, []);

  const fetchImpactData = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
      toast.error("Please sign in to view your certificate");
      navigate('/login');
      return;
    }

    setLoading(true);
    try {
      const response = await api.get(`/api/users/${userId}/impact`);
      setImpact(response.data);
    } catch (err) {
      console.error(err);
      toast.error(t('common.error'));
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return <div className="no-data">{t('common.loading')}</div>;
  }

  if (!impact) {
    return <div className="no-data">No impact stats found for your account.</div>;
  }

  const isDonor = impact.userRole === 'DONOR';

  return (
    <div className="certificate-page-container">
      {/* Navigation Header (hidden during printing) */}
      <div className="certificate-nav-header no-print">
        <button
          onClick={() => {
            const role = localStorage.getItem('role');
            navigate(role === 'DONOR' ? '/donor/dashboard' : '/ngo/dashboard');
          }}
          className="btn btn-secondary"
        >
          &larr; {t('impact.backDashboard', 'Back to Dashboard')}
        </button>
        <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <ThemeToggle />
          <button onClick={handlePrint} className="btn btn-primary">
            🖨️ {t('impact.printCert', 'Download / Print Certificate')}
          </button>
        </div>
      </div>

      {/* Printable Certificate Frame */}
      <div className="certificate-frame">
        <div className="certificate-border-inner">
          <div className="certificate-content-wrap">
            
            <div className="certificate-header">
              <div className="cert-logo">🌿</div>
              <h2>{t('impact.certTitle', 'Certificate of Environmental Impact')}</h2>
              <p className="cert-subtitle">{t('impact.certSubtitle', 'Awarded by the FoodBridge Network')}</p>
            </div>

            <div className="cert-body">
              <p className="presented-to">{t('impact.presentedTo', 'This certificate is proudly presented to')}</p>
              <h1 className="recipient-name">{impact.userName}</h1>
              <p className="recipient-details">{t('auth.city', 'City')}: {impact.city} | {t('auth.role', 'Role')}: {impact.userRole}</p>

              <div className="cert-divider"></div>

              <p className="achievement-text">
                {t('impact.achievement', 'In recognition of outstanding dedication to minimizing neighborhood food waste, alleviating urban hunger, and actively reducing environmental carbon emissions.')}
              </p>

              {/* Stats Counters */}
              <div className="cert-stats-grid">
                <div className="cert-stat-item">
                  <h3>{impact.totalMeals}</h3>
                  <p>{isDonor ? t('impact.mealsDonated', 'Meals Donated') : t('impact.mealsRescued', 'Meals Rescued')}</p>
                </div>
                <div className="cert-stat-item">
                  <h3>{impact.co2SavedKg.toFixed(1)} kg</h3>
                  <p>{t('impact.co2Prevented', 'CO₂ Emissions Prevented')}</p>
                </div>
                <div className="cert-stat-item">
                  <h3>{impact.treesEquivalent}</h3>
                  <p>{t('impact.treesEquivalent', 'Annual Tree Equivalents')}</p>
                </div>
              </div>

              <p className="trust-notation">
                {t('impact.trustRating', 'Verified platform Integrity Rating')}: <strong>{impact.trustScore.toFixed(1)} / 5.0</strong>
              </p>
            </div>

            <div className="certificate-footer">
              <div className="sign-block">
                <div className="signature-line signature-font">Antigravity AI</div>
                <p>Platform Coordinator</p>
              </div>
              
              <div className="seal-block">
                <div className="cert-seal">
                  <span>OFFICIAL</span>
                  <span>SEAL</span>
                </div>
              </div>

              <div className="sign-block">
                <div className="signature-line signature-font">FoodBridge Org</div>
                <p>Network Validator</p>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

export default ImpactCertificate;
