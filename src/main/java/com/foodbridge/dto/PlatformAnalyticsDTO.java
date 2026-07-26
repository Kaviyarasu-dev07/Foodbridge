package com.foodbridge.dto;

import java.util.Map;

public class PlatformAnalyticsDTO {
    private Map<String, Integer> listingsPerDay;
    private double claimRatePercentage;
    private double averageTimeToClaimMinutes;

    public PlatformAnalyticsDTO() {
    }

    public PlatformAnalyticsDTO(Map<String, Integer> listingsPerDay, double claimRatePercentage, double averageTimeToClaimMinutes) {
        this.listingsPerDay = listingsPerDay;
        this.claimRatePercentage = claimRatePercentage;
        this.averageTimeToClaimMinutes = averageTimeToClaimMinutes;
    }

    public Map<String, Integer> getListingsPerDay() {
        return listingsPerDay;
    }

    public void setListingsPerDay(Map<String, Integer> listingsPerDay) {
        this.listingsPerDay = listingsPerDay;
    }

    public double getClaimRatePercentage() {
        return claimRatePercentage;
    }

    public void setClaimRatePercentage(double claimRatePercentage) {
        this.claimRatePercentage = claimRatePercentage;
    }

    public double getAverageTimeToClaimMinutes() {
        return averageTimeToClaimMinutes;
    }

    public void setAverageTimeToClaimMinutes(double averageTimeToClaimMinutes) {
        this.averageTimeToClaimMinutes = averageTimeToClaimMinutes;
    }
}
