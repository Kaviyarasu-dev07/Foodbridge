import React, { useState, useEffect } from 'react';

const CountdownTimer = ({ expiresAt }) => {
  const [timeLeft, setTimeLeft] = useState('');
  const [isUrgent, setIsUrgent] = useState(false);
  const [isExpired, setIsExpired] = useState(false);

  useEffect(() => {
    if (!expiresAt) {
      setTimeLeft('N/A');
      return;
    }

    const calculateTime = () => {
      const targetTime = new Date(expiresAt).getTime();
      const now = new Date().getTime();
      const difference = targetTime - now;

      if (difference <= 0) {
        setTimeLeft('Expired');
        setIsExpired(true);
        setIsUrgent(false);
        return true; // indicator to clear interval
      }

      const hours = Math.floor(difference / (1000 * 60 * 60));
      const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((difference % (1000 * 60)) / 1000);

      // Flag urgent if difference is less than 15 minutes (15 * 60 * 1000)
      if (difference < 15 * 60 * 1000) {
        setIsUrgent(true);
      } else {
        setIsUrgent(false);
      }

      let timeString = '';
      if (hours > 0) {
        timeString += `${hours} hr `;
      }
      timeString += `${minutes} min ${seconds} sec`;

      setTimeLeft(timeString);
      setIsExpired(false);
      return false;
    };

    // Run immediately
    const shouldStop = calculateTime();
    if (shouldStop) return;

    const intervalId = setInterval(() => {
      const stop = calculateTime();
      if (stop) {
        clearInterval(intervalId);
      }
    }, 1000);

    return () => clearInterval(intervalId);
  }, [expiresAt]);

  const getStyle = () => {
    if (isExpired) {
      return { color: '#dc3545', fontWeight: 'bold' };
    }
    if (isUrgent) {
      return { color: '#dc3545', fontWeight: '600', animation: 'pulse 1.5s infinite' };
    }
    return { color: '#1D9E75', fontWeight: '500' };
  };

  return <span style={getStyle()}>{timeLeft}</span>;
};

export default CountdownTimer;
