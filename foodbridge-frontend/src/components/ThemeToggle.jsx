import React, { useState, useEffect } from 'react';
import { Sun, Moon, Contrast } from 'lucide-react';

const ThemeToggle = () => {
  const [theme, setTheme] = useState('light');

  useEffect(() => {
    // Check for saved theme
    const savedTheme = localStorage.getItem('foodbridge_theme') || 'light';
    setTheme(savedTheme);
    document.documentElement.setAttribute('data-theme', savedTheme);
  }, []);

  const cycleTheme = () => {
    const themes = ['light', 'dark', 'hc'];
    const currentIndex = themes.indexOf(theme);
    const nextTheme = themes[(currentIndex + 1) % themes.length];
    
    setTheme(nextTheme);
    localStorage.setItem('foodbridge_theme', nextTheme);
    document.documentElement.setAttribute('data-theme', nextTheme);
  };

  const getThemeIcon = () => {
    if (theme === 'dark') return <Moon size={20} />;
    if (theme === 'hc') return <Contrast size={20} />;
    return <Sun size={20} />;
  };

  const getThemeTitle = () => {
    if (theme === 'dark') return 'Dark Mode';
    if (theme === 'hc') return 'High Contrast Mode';
    return 'Light Mode';
  };

  return (
    <div className="theme-toggle">
      <button
        onClick={cycleTheme}
        className="btn-theme-cycle"
        title={getThemeTitle()}
        aria-label="Toggle Theme"
      >
        <span className="theme-icon">{getThemeIcon()}</span>
      </button>
    </div>
  );
};

export default ThemeToggle;
