package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long listingId;

    @Column(nullable = false, name = "sender_id")
    private Long senderId;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String senderRole; // DONOR or NGO

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public ChatMessage() {
    }

    public ChatMessage(Long id, Long listingId, Long senderId, String senderName, String senderRole, String message, boolean isRead, LocalDateTime sentAt) {
        this.id = id;
        this.listingId = listingId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.isRead = isRead;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean getIsRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }

    public static class ChatMessageBuilder {
        private Long id;
        private Long listingId;
        private Long senderId;
        private String senderName;
        private String senderRole;
        private String message;
        private boolean isRead = false;
        private LocalDateTime sentAt;

        ChatMessageBuilder() {}

        public ChatMessageBuilder id(Long id) { this.id = id; return this; }
        public ChatMessageBuilder listingId(Long listingId) { this.listingId = listingId; return this; }
        public ChatMessageBuilder senderId(Long senderId) { this.senderId = senderId; return this; }
        public ChatMessageBuilder senderName(String senderName) { this.senderName = senderName; return this; }
        public ChatMessageBuilder senderRole(String senderRole) { this.senderRole = senderRole; return this; }
        public ChatMessageBuilder message(String message) { this.message = message; return this; }
        public ChatMessageBuilder isRead(boolean isRead) { this.isRead = isRead; return this; }
        public ChatMessageBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }

        public ChatMessage build() {
            return new ChatMessage(id, listingId, senderId, senderName, senderRole, message, isRead, sentAt);
        }
    }
}
