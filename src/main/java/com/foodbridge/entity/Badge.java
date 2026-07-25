package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @Column(nullable = false)
    private String badgeName;

    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    public Badge() {
    }

    public Badge(Long id, User user, String badgeName, String description, LocalDateTime earnedAt) {
        this.id = id;
        this.user = user;
        this.badgeName = badgeName;
        this.description = description;
        this.earnedAt = earnedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }

    public static BadgeBuilder builder() {
        return new BadgeBuilder();
    }

    public static class BadgeBuilder {
        private Long id;
        private User user;
        private String badgeName;
        private String description;
        private LocalDateTime earnedAt;

        BadgeBuilder() {}

        public BadgeBuilder id(Long id) { this.id = id; return this; }
        public BadgeBuilder user(User user) { this.user = user; return this; }
        public BadgeBuilder badgeName(String badgeName) { this.badgeName = badgeName; return this; }
        public BadgeBuilder description(String description) { this.description = description; return this; }
        public BadgeBuilder earnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; return this; }

        public Badge build() {
            return new Badge(id, user, badgeName, description, earnedAt);
        }
    }
}
