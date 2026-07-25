import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import ThemeToggle from '../components/ThemeToggle';
import AnimatedCounter from '../components/AnimatedCounter';

const HomePage = () => {
  const { t } = useTranslation();
  const [impactData, setImpactData] = useState({
    totalMealsRescued: 15420,
    totalCO2SavedKg: 5820.5,
    treesEquivalent: 240,
    waterSavedLitres: 45000,
    todayMeals: 342,
    activeDonors: 48,
    activeNGOs: 32,
    lastUpdated: null,
  });

  useEffect(() => {
    // Connect stats to real API calls from GET /api/public/impact endpoint
    const fetchImpact = async () => {
      try {
        const response = await fetch('/api/public/impact');
        if (response.ok) {
          const data = await response.json();
          setImpactData(data);
        }
      } catch (err) {
        console.error("Failed to fetch live impact data, using fallback live values:", err);
      }
    };
    fetchImpact();
    const interval = setInterval(fetchImpact, 10000);
    return () => clearInterval(interval);
  }, []);

  const activeRescuesCount = impactData.activeDonors + impactData.activeNGOs;

  const foodCards = [
    {
      id: 1,
      foodName: "Fresh Veg Biryani & Meals",
      quantity: "25 Packets",
      foodType: "COOKED MEAL",
      condition: "FRESH",
      location: "Guindy, Chennai",
      expiresIn: "45 mins",
      donorName: "Annam Restaurant",
      gradClass: "green-grad",
      icon: "🍛"
    },
    {
      id: 2,
      foodName: "Assorted Breads & Buns",
      quantity: "12 Kgs",
      foodType: "BAKERY",
      condition: "GOOD",
      location: "T-Nagar, Chennai",
      expiresIn: "1.5 hours",
      donorName: "Super Bakery",
      gradClass: "amber-grad",
      icon: "🥐"
    },
    {
      id: 3,
      foodName: "Farm Vegetables & Greens",
      quantity: "35 Kgs",
      foodType: "RAW PRODUCE",
      condition: "FRESH",
      location: "Koyambedu Market, Chennai",
      expiresIn: "4 hours",
      donorName: "Kavin Veg Mart",
      gradClass: "purple-grad",
      icon: "🥗"
    }
  ];

  const features = [
    {
      title: "Real-Time Matching",
      desc: "Instant WebSocket alerts connect surplus food donors with the nearest active NGOs the second food is posted.",
      tech: "STOMP WebSockets",
      color: "blue",
      icon: "⚡"
    },
    {
      title: "AI Route Optimizer",
      desc: "Calculates the absolute fastest multi-stop collection paths for volunteer drivers using advanced geographic nearest-neighbour matrices.",
      tech: "Route Optimizer",
      color: "green",
      icon: "🗺️"
    },
    {
      title: "Predictive Surplus Alerts",
      desc: "Advanced algorithmic patterns analyze recurring donor behaviors to alert NGOs of incoming surplus food before it is even officially listed.",
      tech: "AI Estimator",
      color: "purple",
      icon: "🔮"
    },
    {
      title: "Interactive Geolocation",
      desc: "Pinpoints exact rescue locations, donor kitchens, and hunger spots across the city with dynamic mapping integrations.",
      tech: "Leaflet Maps",
      color: "amber",
      icon: "📍"
    },
    {
      title: "Progressive Web App",
      desc: "Install directly to your home screen for ultra-fast native access, background syncing, and real-time push notification updates.",
      tech: "Vite PWA",
      color: "rose",
      icon: "📱"
    },
    {
      title: "Robust Secure Backend",
      desc: "Engineered for high concurrency, rock-solid transactional safety, and comprehensive audit logging for complete transparency.",
      tech: "Spring Boot",
      color: "teal",
      icon: "🛡️"
    }
  ];

  return (
    <div className="clean-homepage">
      {/* 1. Navbar */}
      <nav className="clean-navbar">
        <div className="clean-navbar-left">
          <Link to="/" className="clean-logo-group">
            <div className="clean-logo-box">🌿</div>
            <span className="clean-logo-text">FoodBridge</span>
          </Link>
          <div className="clean-live-pill">
            <span className="clean-live-dot"></span>
            {activeRescuesCount} Active Rescues
          </div>
        </div>
        <div className="clean-navbar-actions">
          <ThemeToggle />
          <Link to="/login" className="btn btn-ghost">{t('nav.signIn', 'Sign in')}</Link>
          <Link to="/register" className="btn btn-primary">{t('nav.getStarted', 'Get started')}</Link>
        </div>
      </nav>

      {/* 2. Hero Section */}
      <section className="clean-hero">
        <div className="clean-hero-badge">✨ Real-time food rescue · Chennai</div>
        <h1>Surplus food to hungry people in 60 minutes</h1>
        <p className="clean-hero-subtext">
          Directly connecting restaurants, event organizers, and cloud kitchens with verified NGOs to eliminate food waste and solve urban hunger instantly.
        </p>
        <div className="clean-hero-actions">
          <Link to="/register" className="btn btn-primary btn-large">Post surplus food</Link>
          <Link to="/login" className="btn btn-ghost btn-large">I'm an NGO &rarr;</Link>
        </div>
        <div className="clean-trust-row">
          <div className="clean-trust-item">
            <span className="clean-trust-icon">🛡️</span>
            <span>FSSAI Safety Verified</span>
          </div>
          <div className="clean-trust-item">
            <span className="clean-trust-icon">💚</span>
            <span>100% Volunteer Network</span>
          </div>
          <div className="clean-trust-item">
            <span className="clean-trust-icon">🌿</span>
            <span>Zero Waste Mission</span>
          </div>
        </div>
      </section>

      {/* 3. Impact Bar */}
      <section className="clean-impact-section">
        <div className="clean-impact-grid">
          <div className="clean-impact-cell">
            <div className="clean-impact-label">🍽️ Total Rescued</div>
            <div className="clean-impact-num">
              <AnimatedCounter value={impactData.totalMealsRescued} duration={1500} />
            </div>
            <div className="clean-impact-sub">Warm meals shared</div>
          </div>
          <div className="clean-impact-cell">
            <div className="clean-impact-label">🌱 Carbon Offset</div>
            <div className="clean-impact-num">
              <AnimatedCounter value={impactData.totalCO2SavedKg} duration={1500} suffix=" kg" />
            </div>
            <div className="clean-impact-sub">Methane & CO₂ prevented</div>
          </div>
          <div className="clean-impact-cell">
            <div className="clean-impact-label">⏰ Today's Impact</div>
            <div className="clean-impact-num">
              <AnimatedCounter value={impactData.todayMeals} duration={1500} />
            </div>
            <div className="clean-impact-sub">Portions rescued today</div>
          </div>
          <div className="clean-impact-cell">
            <div className="clean-impact-label">🤝 Active NGOs</div>
            <div className="clean-impact-num">
              <AnimatedCounter value={impactData.activeNGOs} duration={1500} />
            </div>
            <div className="clean-impact-sub">On-ground rescue partners</div>
          </div>
        </div>
      </section>

      {/* 4. Food Cards Section */}
      <section className="clean-food-section">
        <div className="clean-section-header">
          <h2>Live Surplus Food Near You</h2>
          <p>Real-time donations currently awaiting pickup by registered volunteer drivers.</p>
        </div>
        <div className="clean-food-grid">
          {foodCards.map((item) => (
            <div key={item.id} className="clean-food-card">
              <div className={`clean-card-img-area ${item.gradClass}`}>
                <div className="clean-card-timer">⏰ Expires in {item.expiresIn}</div>
                <div className="clean-card-icon">{item.icon}</div>
              </div>
              <div className="clean-card-body">
                <h3 className="clean-card-title">{item.foodName}</h3>
                <div className="clean-card-meta">
                  <span><strong>{t('listing.quantity', 'Quantity')}:</strong> {item.quantity}</span>
                  <span><strong>{t('donor.condition', 'Condition')}:</strong> <span className={`cond-${item.condition.toLowerCase()}`}>{item.condition}</span></span>
                  <span><strong>{t('listing.location', 'Location')}:</strong> {item.location}</span>
                </div>
                <div className="clean-card-footer">
                  <span className="clean-card-donor">{item.donorName}</span>
                  <Link to="/login" className="clean-claim-btn">{t('listing.claimNow', 'Claim Now')} &rarr;</Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* 5. How It Works Section */}
      <section className="clean-how-section">
        <div className="clean-section-header">
          <h2>How FoodBridge Works</h2>
          <p>Seamless three-step rescue operations engineered for extreme speed and food safety.</p>
        </div>
        <div className="clean-how-grid">
          <div className="clean-how-step">
            <div className="clean-step-num">1</div>
            <div className="clean-step-icon-box">🥘</div>
            <h4 className="clean-step-title">Donor Posts Surplus</h4>
            <p className="clean-step-desc">
              Restaurants or event hosts list excess fresh food in 30 seconds. Our platform automatically tags geolocation and sets rigorous safety expiration timers.
            </p>
          </div>
          <div className="clean-how-step">
            <div className="clean-step-num">2</div>
            <div className="clean-step-icon-box">⚡</div>
            <h4 className="clean-step-title">Instant NGO Alert</h4>
            <p className="clean-step-desc">
              Nearby verified NGOs receive instant WebSocket notifications. Volunteers review portion details, dietary categories, and claim the food instantly.
            </p>
          </div>
          <div className="clean-how-step">
            <div className="clean-step-num">3</div>
            <div className="clean-step-icon-box">🚀</div>
            <h4 className="clean-step-title">Optimized Pickup</h4>
            <p className="clean-step-desc">
              Our integrated AI Route Optimizer coordinates the absolute fastest collection path. The food reaches hungry citizens in under 60 minutes.
            </p>
          </div>
        </div>
      </section>

      {/* 6. Features Grid */}
      <section className="clean-features-section">
        <div className="clean-section-header">
          <h2>Powered by Advanced Technology</h2>
          <p>State-of-the-art architecture built to solve complex logistical distribution challenges.</p>
        </div>
        <div className="clean-features-grid">
          {features.map((feat, idx) => (
            <div key={idx} className="clean-feature-card">
              <div className="clean-feature-top">
                <div className={`clean-feature-icon-box ${feat.color}`}>
                  {feat.icon}
                </div>
                <h4 className="clean-feature-title">{feat.title}</h4>
                <p className="clean-feature-desc">{feat.desc}</p>
              </div>
              <div className="clean-tech-pill">{feat.tech}</div>
            </div>
          ))}
        </div>
      </section>

      {/* 7. Dark Green CTA Section */}
      <section className="clean-cta-section">
        <div className="clean-cta-content">
          <h2 className="clean-cta-title">Join the Real-Time Food Rescue Movement</h2>
          <p className="clean-cta-sub">
            Whether you manage a commercial kitchen or lead a hunger relief charity, your participation directly saves lives and protects our planet.
          </p>
          <div className="clean-cta-actions">
            <Link to="/register" className="btn btn-white btn-large">Get Started Now</Link>
            <Link to="/login" className="btn btn-outline-light btn-large">Sign In to Portal</Link>
          </div>
        </div>
      </section>

      {/* 8. Clean Footer */}
      <footer className="clean-footer">
        <div className="clean-footer-grid">
          <div className="clean-footer-brand">
            <Link to="/" className="clean-logo-group">
              <div className="clean-logo-box">🌿</div>
              <span className="clean-logo-text">FoodBridge</span>
            </Link>
            <p>
              An advanced real-time surplus food redistribution network bridging the gap between food abundance and urban hunger across Chennai.
            </p>
          </div>
          <div className="clean-footer-col">
            <h4>Solutions</h4>
            <ul>
              <li><Link to="/register">For Restaurants</Link></li>
              <li><Link to="/register">For NGOs & Charities</Link></li>
              <li><Link to="/register">Volunteer Drivers</Link></li>
              <li><Link to="/leaderboard">Live Impact Matrix</Link></li>
            </ul>
          </div>
          <div className="clean-footer-col">
            <h4>Technology</h4>
            <ul>
              <li><a href="#">STOMP WebSockets</a></li>
              <li><a href="#">AI Route Optimizer</a></li>
              <li><a href="#">Predictive Pattern ML</a></li>
              <li><a href="#">Spring Boot Architecture</a></li>
            </ul>
          </div>
          <div className="clean-footer-col">
            <h4>Legal & Safety</h4>
            <ul>
              <li><a href="#">FSSAI Guidelines</a></li>
              <li><a href="#">Good Samaritan Law</a></li>
              <li><a href="#">Privacy Policy</a></li>
              <li><a href="#">Terms of Service</a></li>
            </ul>
          </div>
        </div>
        <div className="clean-footer-bottom">
          <p>&copy; 2026 FoodBridge Network. All rights reserved.</p>
          <div className="clean-footer-tech">
            <span>Built with React 19 & Spring Boot 2.7</span>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default HomePage;

