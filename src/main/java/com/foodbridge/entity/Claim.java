package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "listing_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FoodListing listing;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ngo_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User ngo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime claimedAt;

    private LocalDateTime pickedUpAt;
    private Integer rating;

    @Column(length = 1000)
    private String review;

    public Claim() {
    }

    public Claim(Long id, FoodListing listing, User ngo, ClaimStatus status, LocalDateTime claimedAt, LocalDateTime pickedUpAt, Integer rating, String review) {
        this.id = id;
        this.listing = listing;
        this.ngo = ngo;
        this.status = status;
        this.claimedAt = claimedAt;
        this.pickedUpAt = pickedUpAt;
        this.rating = rating;
        this.review = review;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FoodListing getListing() { return listing; }
    public void setListing(FoodListing listing) { this.listing = listing; }
    public User getNgo() { return ngo; }
    public void setNgo(User ngo) { this.ngo = ngo; }
    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public static ClaimBuilder builder() {
        return new ClaimBuilder();
    }

    public static class ClaimBuilder {
        private Long id;
        private FoodListing listing;
        private User ngo;
        private ClaimStatus status;
        private LocalDateTime claimedAt;
        private LocalDateTime pickedUpAt;
        private Integer rating;
        private String review;

        ClaimBuilder() {}

        public ClaimBuilder id(Long id) { this.id = id; return this; }
        public ClaimBuilder listing(FoodListing listing) { this.listing = listing; return this; }
        public ClaimBuilder ngo(User ngo) { this.ngo = ngo; return this; }
        public ClaimBuilder status(ClaimStatus status) { this.status = status; return this; }
        public ClaimBuilder claimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; return this; }
        public ClaimBuilder pickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; return this; }
        public ClaimBuilder rating(Integer rating) { this.rating = rating; return this; }
        public ClaimBuilder review(String review) { this.review = review; return this; }

        public Claim build() {
            return new Claim(id, listing, ngo, status, claimedAt, pickedUpAt, rating, review);
        }
    }

    public enum ClaimStatus {
        CLAIMED, PICKED_UP, CANCELLED
    }
}
