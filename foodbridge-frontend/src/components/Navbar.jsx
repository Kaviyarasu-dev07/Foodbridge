import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import ThemeToggle from './ThemeToggle';

const Navbar = () => {
  const { t } = useTranslation();

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">
          <span className="brand-title">FoodBridge</span>
        </Link>
      </div>
      <div className="navbar-menu">
        <Link to="/#how-it-works" className="nav-link">{t('nav.howItWorks')}</Link>
        <Link to="/register" className="nav-link">{t('nav.forNGOs')}</Link>
        <Link to="/leaderboard" className="nav-link">{t('nav.impact')}</Link>
      </div>
      <div className="navbar-actions">
        <ThemeToggle />
        <Link to="/login" className="btn btn-secondary btn-small">{t('nav.signIn')}</Link>
        <Link to="/register" className="btn btn-primary btn-small">{t('nav.getStarted')}</Link>
      </div>
    </nav>
  );
};

export default Navbar;
