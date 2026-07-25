package com.foodbridge.dto;

public class DonorPatternDTO {
    private Long donorId;
    private String donorName;
    private String donorLocation;
    private int dayOfWeek; // 1-7
    private String dayName; // "Saturday"
    private int hourOfDay; // 0-23
    private int averageQuantity;
    private String commonFoodType;
    private int occurrences;
    private Double confidenceScore;
    private Double latitude;
    private Double longitude;

    public DonorPatternDTO() {
    }

    public DonorPatternDTO(Long donorId, String donorName, String donorLocation, int dayOfWeek, String dayName, int hourOfDay, int averageQuantity, String commonFoodType, int occurrences, Double confidenceScore, Double latitude, Double longitude) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorLocation = donorLocation;
        this.dayOfWeek = dayOfWeek;
        this.dayName = dayName;
        this.hourOfDay = hourOfDay;
        this.averageQuantity = averageQuantity;
        this.commonFoodType = commonFoodType;
        this.occurrences = occurrences;
        this.confidenceScore = confidenceScore;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorLocation() {
        return donorLocation;
    }

    public void setDonorLocation(String donorLocation) {
        this.donorLocation = donorLocation;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getAverageQuantity() {
        return averageQuantity;
    }

    public void setAverageQuantity(int averageQuantity) {
        this.averageQuantity = averageQuantity;
    }

    public String getCommonFoodType() {
        return commonFoodType;
    }

    public void setCommonFoodType(String commonFoodType) {
        this.commonFoodType = commonFoodType;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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
}
