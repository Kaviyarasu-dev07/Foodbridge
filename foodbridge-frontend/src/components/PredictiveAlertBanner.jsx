import React from 'react';
import toast from 'react-hot-toast';

const PredictiveAlertBanner = ({ alert }) => {
  if (!alert || !alert.pattern) return null;

  const { donorName, commonFoodType, averageQuantity, hourOfDay } = alert.pattern;
  const formatHour = (hour) => {
    const period = hour >= 12 ? 'PM' : 'AM';
    const h = hour % 12 || 12;
    return `${h}:00 ${period}`;
  };

  const expectedTime = formatHour(hourOfDay);
  const foodStr = commonFoodType ? commonFoodType.replace('_', ' ') : 'Food';

  const handleSetReminder = () => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Reminder Set!', {
        body: `We will notify you 5 mins before ${donorName} posts ${foodStr}.`,
        icon: '/manifest-icon-192.png'
      });
    }
    toast.success(`⏰ Reminder set! We will notify you 5 mins before ${expectedTime}.`);
  };

  return (
    <div className="predictive-alert-banner">
      <div className="predictive-banner-left">
        <div className="banner-clock-icon">
          ⏱️
        </div>
        <div className="predictive-banner-info">
          <h4>
            <span style={{ backgroundColor: '#ffc107', padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', color: '#000' }}>Expected soon</span>
            {donorName} is expected to post surplus food soon!
          </h4>
          <p>Estimated: <strong>{averageQuantity} packets of {foodStr}</strong> around <strong>{expectedTime}</strong></p>
        </div>
      </div>
      <button onClick={handleSetReminder} className="btn-reminder">
        🔔 Set reminder
      </button>
    </div>
  );
};

export default PredictiveAlertBanner;
