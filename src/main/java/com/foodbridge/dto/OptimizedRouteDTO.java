package com.foodbridge.dto;

import java.util.List;

public class OptimizedRouteDTO {
    private List<RouteStopDTO> stops;
    private Double totalDistanceKm;
    private int estimatedTotalMinutes;
    private String googleMapsUrl;

    public OptimizedRouteDTO() {
    }

    public OptimizedRouteDTO(List<RouteStopDTO> stops, Double totalDistanceKm, int estimatedTotalMinutes, String googleMapsUrl) {
        this.stops = stops;
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedTotalMinutes = estimatedTotalMinutes;
        this.googleMapsUrl = googleMapsUrl;
    }

    public List<RouteStopDTO> getStops() {
        return stops;
    }

    public void setStops(List<RouteStopDTO> stops) {
        this.stops = stops;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public int getEstimatedTotalMinutes() {
        return estimatedTotalMinutes;
    }

    public void setEstimatedTotalMinutes(int estimatedTotalMinutes) {
        this.estimatedTotalMinutes = estimatedTotalMinutes;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }
}
