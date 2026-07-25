package com.foodbridge.entity;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String endpoint;

    @Column(nullable = false, length = 500)
    private String p256dh;

    @Column(nullable = false, length = 200)
    private String auth;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PushSubscription() {
    }

    public PushSubscription(Long id, User user, String endpoint, String p256dh, String auth, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getP256dh() { return p256dh; }
    public void setP256dh(String p256dh) { this.p256dh = p256dh; }

    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static PushSubscriptionBuilder builder() {
        return new PushSubscriptionBuilder();
    }

    public static class PushSubscriptionBuilder {
        private Long id;
        private User user;
        private String endpoint;
        private String p256dh;
        private String auth;
        private LocalDateTime createdAt = LocalDateTime.now();

        PushSubscriptionBuilder() {}

        public PushSubscriptionBuilder id(Long id) { this.id = id; return this; }
        public PushSubscriptionBuilder user(User user) { this.user = user; return this; }
        public PushSubscriptionBuilder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public PushSubscriptionBuilder p256dh(String p256dh) { this.p256dh = p256dh; return this; }
        public PushSubscriptionBuilder auth(String auth) { this.auth = auth; return this; }
        public PushSubscriptionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public PushSubscription build() {
            return new PushSubscription(id, user, endpoint, p256dh, auth, createdAt);
        }
    }
}
