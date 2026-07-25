package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "food_sos")
public class FoodSOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private FoodListing listing;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime triggeredAt;

    private LocalDateTime resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SOSStatus status;

    public FoodSOS() {
    }

    public FoodSOS(Long id, FoodListing listing, LocalDateTime triggeredAt, LocalDateTime resolvedAt, SOSStatus status) {
        this.id = id;
        this.listing = listing;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FoodListing getListing() { return listing; }
    public void setListing(FoodListing listing) { this.listing = listing; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public SOSStatus getStatus() { return status; }
    public void setStatus(SOSStatus status) { this.status = status; }

    public static FoodSOSBuilder builder() {
        return new FoodSOSBuilder();
    }

    public static class FoodSOSBuilder {
        private Long id;
        private FoodListing listing;
        private LocalDateTime triggeredAt;
        private LocalDateTime resolvedAt;
        private SOSStatus status;

        FoodSOSBuilder() {}

        public FoodSOSBuilder id(Long id) { this.id = id; return this; }
        public FoodSOSBuilder listing(FoodListing listing) { this.listing = listing; return this; }
        public FoodSOSBuilder triggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; return this; }
        public FoodSOSBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public FoodSOSBuilder status(SOSStatus status) { this.status = status; return this; }

        public FoodSOS build() {
            return new FoodSOS(id, listing, triggeredAt, resolvedAt, status);
        }
    }

    public enum SOSStatus {
        ACTIVE, RESOLVED
    }
}
