package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "food_listings")
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User donor;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private String quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodType foodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "food_condition")
    private FoodCondition condition;

    @Column(nullable = false)
    private String location;

    private Double latitude;
    private Double longitude;
    private Integer pickupMinutes;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FoodListing() {
    }

    public FoodListing(Long id, User donor, String foodName, String quantity, FoodType foodType, FoodCondition condition,
                       String location, Double latitude, Double longitude, Integer pickupMinutes, LocalDateTime expiresAt,
                       ListingStatus status, String qrCode, LocalDateTime createdAt) {
        this.id = id;
        this.donor = donor;
        this.foodName = foodName;
        this.quantity = quantity;
        this.foodType = foodType;
        this.condition = condition;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pickupMinutes = pickupMinutes;
        this.expiresAt = expiresAt;
        this.status = status;
        this.qrCode = qrCode;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public FoodType getFoodType() { return foodType; }
    public void setFoodType(FoodType foodType) { this.foodType = foodType; }
    public FoodCondition getCondition() { return condition; }
    public void setCondition(FoodCondition condition) { this.condition = condition; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getPickupMinutes() { return pickupMinutes; }
    public void setPickupMinutes(Integer pickupMinutes) { this.pickupMinutes = pickupMinutes; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FoodListingBuilder builder() {
        return new FoodListingBuilder();
    }

    public static class FoodListingBuilder {
        private Long id;
        private User donor;
        private String foodName;
        private String quantity;
        private FoodType foodType;
        private FoodCondition condition;
        private String location;
        private Double latitude;
        private Double longitude;
        private Integer pickupMinutes;
        private LocalDateTime expiresAt;
        private ListingStatus status;
        private String qrCode;
        private LocalDateTime createdAt;

        FoodListingBuilder() {}

        public FoodListingBuilder id(Long id) { this.id = id; return this; }
        public FoodListingBuilder donor(User donor) { this.donor = donor; return this; }
        public FoodListingBuilder foodName(String foodName) { this.foodName = foodName; return this; }
        public FoodListingBuilder quantity(String quantity) { this.quantity = quantity; return this; }
        public FoodListingBuilder foodType(FoodType foodType) { this.foodType = foodType; return this; }
        public FoodListingBuilder condition(FoodCondition condition) { this.condition = condition; return this; }
        public FoodListingBuilder location(String location) { this.location = location; return this; }
        public FoodListingBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public FoodListingBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public FoodListingBuilder pickupMinutes(Integer pickupMinutes) { this.pickupMinutes = pickupMinutes; return this; }
        public FoodListingBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public FoodListingBuilder status(ListingStatus status) { this.status = status; return this; }
        public FoodListingBuilder qrCode(String qrCode) { this.qrCode = qrCode; return this; }
        public FoodListingBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FoodListing build() {
            return new FoodListing(id, donor, foodName, quantity, foodType, condition, location, latitude, longitude, pickupMinutes, expiresAt, status, qrCode, createdAt);
        }
    }

    public enum FoodType {
        COOKED_MEAL, SNACKS, RAW
    }

    public enum FoodCondition {
        FRESH, GOOD, USE_SOON
    }

    public enum ListingStatus {
        ACTIVE, CLAIMED, EXPIRED
    }
}
