package com.foodbridge.dto;

import java.time.LocalDateTime;

public class LiveImpactDTO {
    private long totalMealsRescued;
    private double totalCO2SavedKg;
    private double treesEquivalent;
    private long waterSavedLitres;
    private int todayMeals;
    private int activeDonors;
    private int activeNGOs;
    private LocalDateTime lastUpdated;
    private double co2EquivalentOffsetKg;

    public LiveImpactDTO() {
    }

    public LiveImpactDTO(long totalMealsRescued, double totalCO2SavedKg, double treesEquivalent, long waterSavedLitres,
                          int todayMeals, int activeDonors, int activeNGOs, LocalDateTime lastUpdated, double co2EquivalentOffsetKg) {
        this.totalMealsRescued = totalMealsRescued;
        this.totalCO2SavedKg = totalCO2SavedKg;
        this.treesEquivalent = treesEquivalent;
        this.waterSavedLitres = waterSavedLitres;
        this.todayMeals = todayMeals;
        this.activeDonors = activeDonors;
        this.activeNGOs = activeNGOs;
        this.lastUpdated = lastUpdated;
        this.co2EquivalentOffsetKg = co2EquivalentOffsetKg;
    }

    public long getTotalMealsRescued() { return totalMealsRescued; }
    public void setTotalMealsRescued(long totalMealsRescued) { this.totalMealsRescued = totalMealsRescued; }

    public double getTotalCO2SavedKg() { return totalCO2SavedKg; }
    public void setTotalCO2SavedKg(double totalCO2SavedKg) { this.totalCO2SavedKg = totalCO2SavedKg; }

    public double getTreesEquivalent() { return treesEquivalent; }
    public void setTreesEquivalent(double treesEquivalent) { this.treesEquivalent = treesEquivalent; }

    public long getWaterSavedLitres() { return waterSavedLitres; }
    public void setWaterSavedLitres(long waterSavedLitres) { this.waterSavedLitres = waterSavedLitres; }

    public int getTodayMeals() { return todayMeals; }
    public void setTodayMeals(int todayMeals) { this.todayMeals = todayMeals; }

    public int getActiveDonors() { return activeDonors; }
    public void setActiveDonors(int activeDonors) { this.activeDonors = activeDonors; }

    public int getActiveNGOs() { return activeNGOs; }
    public void setActiveNGOs(int activeNGOs) { this.activeNGOs = activeNGOs; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public double getCo2EquivalentOffsetKg() { return co2EquivalentOffsetKg; }
    public void setCo2EquivalentOffsetKg(double co2EquivalentOffsetKg) { this.co2EquivalentOffsetKg = co2EquivalentOffsetKg; }

    public static LiveImpactDTOBuilder builder() {
        return new LiveImpactDTOBuilder();
    }

    public static class LiveImpactDTOBuilder {
        private long totalMealsRescued;
        private double totalCO2SavedKg;
        private double treesEquivalent;
        private long waterSavedLitres;
        private int todayMeals;
        private int activeDonors;
        private int activeNGOs;
        private LocalDateTime lastUpdated;
        private double co2EquivalentOffsetKg;

        LiveImpactDTOBuilder() {}

        public LiveImpactDTOBuilder totalMealsRescued(long totalMealsRescued) { this.totalMealsRescued = totalMealsRescued; return this; }
        public LiveImpactDTOBuilder totalCO2SavedKg(double totalCO2SavedKg) { this.totalCO2SavedKg = totalCO2SavedKg; return this; }
        public LiveImpactDTOBuilder treesEquivalent(double treesEquivalent) { this.treesEquivalent = treesEquivalent; return this; }
        public LiveImpactDTOBuilder waterSavedLitres(long waterSavedLitres) { this.waterSavedLitres = waterSavedLitres; return this; }
        public LiveImpactDTOBuilder todayMeals(int todayMeals) { this.todayMeals = todayMeals; return this; }
        public LiveImpactDTOBuilder activeDonors(int activeDonors) { this.activeDonors = activeDonors; return this; }
        public LiveImpactDTOBuilder activeNGOs(int activeNGOs) { this.activeNGOs = activeNGOs; return this; }
        public LiveImpactDTOBuilder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public LiveImpactDTOBuilder co2EquivalentOffsetKg(double co2EquivalentOffsetKg) { this.co2EquivalentOffsetKg = co2EquivalentOffsetKg; return this; }

        public LiveImpactDTO build() {
            return new LiveImpactDTO(totalMealsRescued, totalCO2SavedKg, treesEquivalent, waterSavedLitres, todayMeals, activeDonors, activeNGOs, lastUpdated, co2EquivalentOffsetKg);
        }
    }
}
