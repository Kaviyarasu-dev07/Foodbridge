import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const CityImpactChart = () => {
  const [chartData, setChartData] = useState({
    labels: [],
    datasets: [],
  });
  const [loading, setLoading] = useState(true);
  const stompClientRef = useRef(null);

  useEffect(() => {
    fetchCityBreakdown();
    connectWebSocket();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, []);

  const fetchCityBreakdown = async () => {
    try {
      const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
      const response = await fetch(`${baseUrl}/api/public/impact/city`);
      if (response.ok) {
        const data = await response.json();
        
        // Sort keys by value descending
        const sortedEntries = Object.entries(data).sort((a, b) => b[1] - a[1]);
        const labels = sortedEntries.map(entry => entry[0]);
        const values = sortedEntries.map(entry => entry[1]);

        // Generate green shades (dark green to light green)
        const colors = sortedEntries.map((_, index, arr) => {
          const len = arr.length || 1;
          const factor = index / len;
          // Interpolate color from dark green (#0c4832) to theme green (#1D9E75) to light green (#a3cfbb)
          if (factor < 0.5) {
            return `rgba(29, 158, 117, ${1 - factor})`; // Fade down
          } else {
            return `rgba(163, 207, 187, ${1.5 - factor})`;
          }
        });

        setChartData({
          labels,
          datasets: [
            {
              label: 'Meals Rescued',
              data: values,
              backgroundColor: colors,
              borderColor: colors.map(c => c.replace('0.7', '1')),
              borderWidth: 1,
              borderRadius: 8,
              barThickness: 24,
            },
          ],
        });
      }
    } catch (e) {
      console.error("Failed to fetch city breakdown impact:", e);
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    try {
      const socket = new SockJS('/ws');
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
      });

      client.onConnect = () => {
        client.subscribe('/topic/live-impact', () => {
          // Re-fetch breakdown details when live rescue broadcast is caught
          fetchCityBreakdown();
        });
      };

      client.activate();
      stompClientRef.current = client;
    } catch (err) {
      console.error("WebSocket city chart broker connection error:", err);
    }
  };

  const options = {
    indexAxis: 'y', // Makes it horizontal
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: (context) => ` ${context.raw} portions rescued`,
        },
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          font: {
            family: 'Outfit',
            size: 11,
          },
        },
        title: {
          display: true,
          text: 'Meals Rescued',
          font: {
            family: 'Outfit',
            weight: 'bold',
          },
        },
      },
      y: {
        grid: {
          color: 'rgba(0, 0, 0, 0.05)',
        },
        ticks: {
          font: {
            family: 'Outfit',
            size: 12,
            weight: 600,
          },
        },
      },
    },
  };

  return (
    <div className="city-impact-container">
      <h3>Rescue Operations by Chennai Neighborhoods</h3>
      <p className="subtitle">Breakdown of meals rescued per area</p>
      
      {loading ? (
        <div className="chart-loading">
          <div className="spinner"></div>
          <p>Loading neighborhood rescue analysis...</p>
        </div>
      ) : chartData.labels.length === 0 ? (
        <div className="no-chart-data">
          <span className="no-data-icon">📊</span>
          <p>No rescues recorded yet. Be the first to rescue food!</p>
        </div>
      ) : (
        <div className="chart-wrapper" style={{ height: '320px', position: 'relative' }}>
          <Bar data={chartData} options={options} />
        </div>
      )}
    </div>
  );
};

export default CityImpactChart;
