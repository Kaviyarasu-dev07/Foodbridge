package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private FoodListing listing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(Long id, User user, FoodListing listing, NotificationType type, String message, Boolean isRead, LocalDateTime sentAt) {
        this.id = id;
        this.user = user;
        this.listing = listing;
        this.type = type;
        this.message = message;
        this.isRead = isRead != null ? isRead : false;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public FoodListing getListing() { return listing; }
    public void setListing(FoodListing listing) { this.listing = listing; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private Long id;
        private User user;
        private FoodListing listing;
        private NotificationType type;
        private String message;
        private Boolean isRead = false;
        private LocalDateTime sentAt;

        NotificationBuilder() {}

        public NotificationBuilder id(Long id) { this.id = id; return this; }
        public NotificationBuilder user(User user) { this.user = user; return this; }
        public NotificationBuilder listing(FoodListing listing) { this.listing = listing; return this; }
        public NotificationBuilder type(NotificationType type) { this.type = type; return this; }
        public NotificationBuilder message(String message) { this.message = message; return this; }
        public NotificationBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }
        public NotificationBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }

        public Notification build() {
            return new Notification(id, user, listing, type, message, isRead, sentAt);
        }
    }

    public enum NotificationType {
        NEW_FOOD, CLAIMED, EXPIRED, SOS, PREDICTIVE
    }
}
