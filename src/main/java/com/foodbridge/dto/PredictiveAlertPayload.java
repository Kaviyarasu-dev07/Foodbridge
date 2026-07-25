package com.foodbridge.dto;

public class PredictiveAlertPayload {
    private DonorPatternDTO pattern;
    private String message;
    private Double distanceKm;

    public PredictiveAlertPayload() {
    }

    public PredictiveAlertPayload(DonorPatternDTO pattern, String message, Double distanceKm) {
        this.pattern = pattern;
        this.message = message;
        this.distanceKm = distanceKm;
    }

    public DonorPatternDTO getPattern() {
        return pattern;
    }

    public void setPattern(DonorPatternDTO pattern) {
        this.pattern = pattern;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
