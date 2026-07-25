package com.foodbridge.dto;

public class RouteStopDTO {
    private int stopOrder;
    private Long listingId;
    private String foodName;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double distanceFromPrevious; // in km
    private String estimatedArrival; // e.g., "10 min"

    public RouteStopDTO() {
    }

    public RouteStopDTO(int stopOrder, Long listingId, String foodName, String location, Double latitude, Double longitude, Double distanceFromPrevious, String estimatedArrival) {
        this.stopOrder = stopOrder;
        this.listingId = listingId;
        this.foodName = foodName;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromPrevious = distanceFromPrevious;
        this.estimatedArrival = estimatedArrival;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(Double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(String estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }
}
