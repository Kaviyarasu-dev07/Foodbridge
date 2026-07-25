package com.foodbridge.dto;

public class FoodAlertDTO {
    private Long listingId;
    private String foodName;
    private String quantity;
    private String location;
    private Double distanceKm;
    private String expiresAt;
    private String donorName;
    private String type;

    public FoodAlertDTO() {
    }

    public FoodAlertDTO(Long listingId, String foodName, String quantity, String location, Double distanceKm, String expiresAt, String donorName, String type) {
        this.listingId = listingId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.location = location;
        this.distanceKm = distanceKm;
        this.expiresAt = expiresAt;
        this.donorName = donorName;
        this.type = type;
    }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public static FoodAlertDTOBuilder builder() {
        return new FoodAlertDTOBuilder();
    }

    public static class FoodAlertDTOBuilder {
        private Long listingId;
        private String foodName;
        private String quantity;
        private String location;
        private Double distanceKm;
        private String expiresAt;
        private String donorName;
        private String type;

        FoodAlertDTOBuilder() {}

        public FoodAlertDTOBuilder listingId(Long listingId) { this.listingId = listingId; return this; }
        public FoodAlertDTOBuilder foodName(String foodName) { this.foodName = foodName; return this; }
        public FoodAlertDTOBuilder quantity(String quantity) { this.quantity = quantity; return this; }
        public FoodAlertDTOBuilder location(String location) { this.location = location; return this; }
        public FoodAlertDTOBuilder distanceKm(Double distanceKm) { this.distanceKm = distanceKm; return this; }
        public FoodAlertDTOBuilder expiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }
        public FoodAlertDTOBuilder donorName(String donorName) { this.donorName = donorName; return this; }
        public FoodAlertDTOBuilder type(String type) { this.type = type; return this; }

        public FoodAlertDTO build() {
            return new FoodAlertDTO(listingId, foodName, quantity, location, distanceKm, expiresAt, donorName, type);
        }
    }
}
