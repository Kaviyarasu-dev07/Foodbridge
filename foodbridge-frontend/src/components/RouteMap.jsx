import React, { useState } from 'react';
import toast from 'react-hot-toast';

const RouteMap = ({ route, onClose }) => {
  const [pickedUpIds, setPickedUpIds] = useState([]);

  if (!route || !route.stops) return null;

  const handlePickup = (stop) => {
    if (pickedUpIds.includes(stop.listingId)) return;
    setPickedUpIds((prev) => [...prev, stop.listingId]);
    toast.success(`Marked "${stop.foodName}" as picked up!`);
  };

  return (
    <div className="route-modal-overlay" onClick={onClose}>
      <div className="route-modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="route-modal-header">
          <div>
            <h3>🗺️ Optimized Pickup Route</h3>
            <p>Shortest path calculated via Nearest Neighbour AI</p>
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '1.5rem',
              cursor: 'pointer',
              color: 'var(--gray-dark)'
            }}
          >
            ✕
          </button>
        </div>

        <div className="route-stops-list">
          {route.stops.map((stop) => {
            const isPickedUp = pickedUpIds.includes(stop.listingId);
            return (
              <div
                key={stop.listingId}
                className="route-stop-item"
                style={{ opacity: isPickedUp ? 0.7 : 1 }}
              >
                <div className="stop-number-box">
                  {stop.stopOrder}
                </div>

                <div className="stop-details-col">
                  <h4>{stop.foodName}</h4>
                  <p>📍 {stop.location}</p>
                  <div className="stop-meta-row">
                    <span className="stop-meta-tag">🚗 {stop.distanceFromPrevious} km away</span>
                    <span className="stop-meta-tag">⏳ ETA: {stop.estimatedArrival}</span>
                  </div>
                </div>

                <div>
                  {isPickedUp ? (
                    <button className="btn-picked-up" disabled>
                      ✓ Picked Up
                    </button>
                  ) : (
                    <button
                      onClick={() => handlePickup(stop)}
                      className="btn-pickup"
                    >
                      Mark Picked Up
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="route-modal-footer">
          <div className="route-totals">
            <div className="route-total-box">
              <h5>Total Distance</h5>
              <p>{route.totalDistanceKm} km</p>
            </div>
            <div className="route-total-box">
              <h5>Est. Total Time</h5>
              <p>{route.estimatedTotalMinutes} mins</p>
            </div>
          </div>

          <button
            onClick={() => window.open(route.googleMapsUrl, '_blank')}
            className="route-maps-btn"
          >
            🗺️ Open in Google Maps
          </button>
        </div>
      </div>
    </div>
  );
};

export default RouteMap;
