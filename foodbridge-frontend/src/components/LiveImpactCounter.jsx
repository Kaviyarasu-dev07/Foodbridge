import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import AnimatedCounter from './AnimatedCounter';

const LiveImpactCounter = () => {
  const [impactData, setImpactData] = useState({
    totalMealsRescued: 0,
    totalCO2SavedKg: 0.0,
    treesEquivalent: 0.0,
    waterSavedLitres: 0,
    todayMeals: 0,
    activeDonors: 0,
    activeNGOs: 0,
    lastUpdated: null,
  });
  const [wsConnected, setWsConnected] = useState(false);
  const stompClientRef = useRef(null);

  useEffect(() => {
    // 1. Fetch initial platform impact totals
    fetchInitialTotals();

    // 2. Connect WebSocket subscription
    connectWebSocket();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        console.log("Live Impact Counter WebSocket deactivated.");
      }
    };
  }, []);

  const fetchInitialTotals = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/public/impact');
      if (response.ok) {
        const data = await response.json();
        setImpactData(data);
      }
    } catch (err) {
      console.error("Failed to load initial impact totals:", err);
    }
  };

  const connectWebSocket = () => {
    try {
      const socket = new SockJS('http://localhost:8080/ws');
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.onConnect = () => {
        setWsConnected(true);
        console.log("Live Impact Counter WebSocket connected.");

        // Subscribe to live impact updates
        client.subscribe('/topic/live-impact', (message) => {
          try {
            const data = JSON.parse(message.body);
            setImpactData(data);
          } catch (e) {
            console.error("Error parsing live impact update:", e);
          }
        });
      };

      client.onDisconnect = () => {
        setWsConnected(false);
      };

      client.onStompError = (frame) => {
        console.error("STOMP error in LiveImpactCounter:", frame);
      };

      client.activate();
      stompClientRef.current = client;
    } catch (e) {
      console.error("WebSocket connection failure inside LiveImpactCounter:", e);
    }
  };

  const formatTime = (timeStr) => {
    if (!timeStr) return "Never";
    try {
      const date = new Date(timeStr);
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    } catch (e) {
      return "Just now";
    }
  };

  return (
    <div className="live-impact-banner">
      <div className="live-impact-header">
        <div className="live-status-container">
          <span className={`live-pulse-dot ${wsConnected ? 'active' : ''}`}></span>
          <span className="live-status-text">{wsConnected ? 'LIVE FEED' : 'CONNECTED (STATIC)'}</span>
        </div>
        <h2>FoodBridge Live Impact — Updated in Real Time</h2>
        {impactData.lastUpdated && (
          <span className="last-updated-text">
            Last rescue update: {formatTime(impactData.lastUpdated)}
          </span>
        )}
      </div>

      <div className="live-impact-grid">
        <div className="impact-box">
          <span className="box-icon">🍲</span>
          <div className="box-details">
            <h3>
              <AnimatedCounter value={impactData.totalMealsRescued} />
            </h3>
            <p>Meals Rescued</p>
          </div>
        </div>

        <div className="impact-box">
          <span className="box-icon">🌱</span>
          <div className="box-details">
            <h3>
              <AnimatedCounter value={impactData.totalCO2SavedKg} suffix=" kg" />
            </h3>
            <p>CO₂ Emissions Prevented</p>
          </div>
        </div>

        <div className="impact-box">
          <span className="box-icon">🌳</span>
          <div className="box-details">
            <h3>
              <AnimatedCounter value={impactData.treesEquivalent} />
            </h3>
            <p>Tree Absorption Equiv. (Yr)</p>
          </div>
        </div>

        <div className="impact-box">
          <span className="box-icon">💧</span>
          <div className="box-details">
            <h3>
              <AnimatedCounter value={impactData.waterSavedLitres} suffix=" L" />
            </h3>
            <p>Water Footprint Saved</p>
          </div>
        </div>
      </div>

      <div className="live-impact-footer">
        <div className="footer-stat">
          <strong>Today's Rescues:</strong> <AnimatedCounter value={impactData.todayMeals} /> meals
        </div>
        <div className="divider"></div>
        <div className="footer-stat">
          <strong>Active Donors:</strong> <AnimatedCounter value={impactData.activeDonors} />
        </div>
        <div className="divider"></div>
        <div className="footer-stat">
          <strong>Active NGOs:</strong> <AnimatedCounter value={impactData.activeNGOs} />
        </div>
      </div>
    </div>
  );
};

export default LiveImpactCounter;
