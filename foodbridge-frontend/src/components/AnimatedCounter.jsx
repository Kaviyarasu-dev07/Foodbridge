import React, { useEffect, useState, useRef } from 'react';

const AnimatedCounter = ({ value, duration = 1000, suffix = "" }) => {
  const [displayValue, setDisplayValue] = useState(0);
  const previousValueRef = useRef(0);

  useEffect(() => {
    let startTimestamp = null;
    const startVal = previousValueRef.current;
    const endVal = typeof value === 'number' ? value : parseFloat(value) || 0;
    const range = endVal - startVal;

    // If no change, do nothing
    if (range === 0) {
      setDisplayValue(endVal);
      return;
    }

    const step = (timestamp) => {
      if (!startTimestamp) startTimestamp = timestamp;
      const progress = Math.min((timestamp - startTimestamp) / duration, 1);
      
      // Easing out function for smooth landing
      const easeOutQuad = (x) => 1 - (1 - x) * (1 - x);
      const easedProgress = easeOutQuad(progress);

      const currentVal = Math.floor(startVal + (range * easedProgress));
      setDisplayValue(currentVal);

      if (progress < 1) {
        window.requestAnimationFrame(step);
      } else {
        setDisplayValue(endVal);
        previousValueRef.current = endVal;
      }
    };

    window.requestAnimationFrame(step);
  }, [value, duration]);

  const formatNumber = (num) => {
    // Check if integer or double
    if (num % 1 === 0) {
      return num.toLocaleString('en-US');
    }
    return num.toLocaleString('en-US', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  };

  return (
    <span>
      {formatNumber(displayValue)}
      {suffix}
    </span>
  );
};

export default AnimatedCounter;
