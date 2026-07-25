import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { toast } from 'react-hot-toast';
import api from '../services/api';

const RescueChat = ({ listingId, listingTitle, currentUser, onClose, onMessageRead }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);

  const donorQuickReplies = [
    "We have packed the food in clean containers.",
    "Please call when you reach the venue.",
    "Is your volunteer nearby?",
    "Thanks for rescuing this surplus food!"
  ];

  const ngoQuickReplies = [
    "We are on our way to pick up!",
    "Will be there in 15 minutes.",
    "We have reached the location.",
    "Thanks for the wonderful food donation!"
  ];

  const quickReplies = currentUser?.role === 'DONOR' ? donorQuickReplies : ngoQuickReplies;

  useEffect(() => {
    fetchHistoryAndConnect();
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [listingId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const fetchHistoryAndConnect = async () => {
    try {
      // 1. Fetch chat history
      const res = await api.get(`/api/chat/${listingId}/messages`);
      setMessages(res.data);

      // 2. Mark unread messages as read
      if (currentUser?.role) {
        await api.post(`/api/chat/${listingId}/read`, { role: currentUser.role });
        if (onMessageRead) onMessageRead(listingId);
      }

      // 3. Connect to STOMP WebSocket
      const socket = new SockJS('/ws');
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.onConnect = (frame) => {
        setIsConnected(true);
        client.subscribe(`/topic/chat/${listingId}`, (messageFrame) => {
          const payload = JSON.parse(messageFrame.body);
          if (payload.type === 'READ_RECEIPT') {
            setMessages(prev => prev.map(m => ({ ...m, isRead: true })));
          } else {
            setMessages(prev => {
              // Avoid duplicate if REST already added it or STOMP arrives
              if (prev.some(m => m.id === payload.id)) return prev;
              return [...prev, payload];
            });
            // If incoming message is from the other party, mark as read
            if (payload.senderRole !== currentUser?.role) {
              api.post(`/api/chat/${listingId}/read`, { role: currentUser?.role }).catch(e => console.error(e));
              if (onMessageRead) onMessageRead(listingId);
            }
          }
        });
      };

      client.onStompError = (frame) => {
        console.error('STOMP Error:', frame);
      };

      client.activate();
      stompClientRef.current = client;

    } catch (err) {
      console.error("Failed to initialize chat:", err);
      toast.error("Could not load chat history.");
    }
  };

  const handleSend = async (textToSend) => {
    const messageText = textToSend || input;
    if (!messageText.trim()) return;

    const messagePayload = {
      listingId,
      senderId: currentUser.id,
      senderName: currentUser.name,
      senderRole: currentUser.role,
      message: messageText.trim()
    };

    try {
      if (!textToSend) setInput('');
      // Use REST endpoint for reliable broadcasting and persistence
      await api.post(`/api/chat/${listingId}/send`, messagePayload);
    } catch (err) {
      console.error("Failed to send message:", err);
      toast.error("Failed to send message. Please try again.");
    }
  };

  const formatTime = (timeStr) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="chat-overlay" onClick={onClose}>
      <div className="chat-side-panel" onClick={(e) => e.stopPropagation()}>
        <div className="chat-header">
          <div className="chat-header-info">
            <h3>{listingTitle || 'Rescue Chat'}</h3>
            <div className="online-indicator">
              <span className={`online-dot ${isConnected ? 'connected' : 'disconnected'}`}></span>
              {isConnected ? 'Online • Real-time' : 'Connecting...'}
            </div>
          </div>
          <button className="close-chat-btn" onClick={onClose} aria-label="Close Chat">
            ✕
          </button>
        </div>

        <div className="chat-messages">
          {messages.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--gray-dark)', margin: 'auto 0' }}>
              No messages yet. Start the conversation to coordinate your food rescue!
            </p>
          ) : (
            messages.map((msg, idx) => {
              const isMe = msg.senderRole === currentUser?.role;
              return (
                <div 
                  key={msg.id || idx} 
                  className={`chat-bubble-wrapper ${isMe ? (currentUser?.role === 'DONOR' ? 'donor' : 'ngo') : (msg.senderRole === 'DONOR' ? 'donor' : 'ngo')}`}
                  style={{ alignSelf: isMe ? 'flex-end' : 'flex-start' }}
                >
                  <span className="chat-sender-name">{msg.senderName} ({msg.senderRole})</span>
                  <div className={`chat-bubble ${isMe ? 'ngo' : 'donor'}`}>
                    {msg.message}
                  </div>
                  <span className="chat-timestamp">
                    {formatTime(msg.sentAt)} {isMe && (msg.isRead ? '• Read' : '• Sent')}
                  </span>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>

        <div className="chat-footer">
          <div className="quick-replies">
            {quickReplies.map((reply, idx) => (
              <button 
                key={idx} 
                className="quick-reply-chip" 
                onClick={() => handleSend(reply)}
              >
                {reply}
              </button>
            ))}
          </div>
          <form 
            className="chat-input-row" 
            onSubmit={(e) => { e.preventDefault(); handleSend(); }}
          >
            <input 
              type="text" 
              placeholder="Type a message..." 
              value={input}
              onChange={(e) => setInput(e.target.value)}
            />
            <button type="submit" className="send-chat-btn" aria-label="Send message">
              ➤
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RescueChat;
