package com.foodbridge.entity;

import javax.persistence.*;

@Entity
@Table(name = "impact_stats")
public class ImpactStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer totalMeals = 0;

    @Column(nullable = false)
    private Integer totalRescues = 0;

    @Column(nullable = false)
    private Double co2SavedKg = 0.0;

    @Column(nullable = false)
    private Integer treesEquivalent = 0;

    @Column(nullable = false, length = 7)
    private String monthYear;

    public ImpactStats() {
    }

    public ImpactStats(Long id, User user, Integer totalMeals, Integer totalRescues, Double co2SavedKg, Integer treesEquivalent, String monthYear) {
        this.id = id;
        this.user = user;
        this.totalMeals = totalMeals != null ? totalMeals : 0;
        this.totalRescues = totalRescues != null ? totalRescues : 0;
        this.co2SavedKg = co2SavedKg != null ? co2SavedKg : 0.0;
        this.treesEquivalent = treesEquivalent != null ? treesEquivalent : 0;
        this.monthYear = monthYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getTotalMeals() { return totalMeals; }
    public void setTotalMeals(Integer totalMeals) { this.totalMeals = totalMeals; }
    public Integer getTotalRescues() { return totalRescues; }
    public void setTotalRescues(Integer totalRescues) { this.totalRescues = totalRescues; }
    public Double getCo2SavedKg() { return co2SavedKg; }
    public void setCo2SavedKg(Double co2SavedKg) { this.co2SavedKg = co2SavedKg; }
    public Integer getTreesEquivalent() { return treesEquivalent; }
    public void setTreesEquivalent(Integer treesEquivalent) { this.treesEquivalent = treesEquivalent; }
    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public static ImpactStatsBuilder builder() {
        return new ImpactStatsBuilder();
    }

    public static class ImpactStatsBuilder {
        private Long id;
        private User user;
        private Integer totalMeals = 0;
        private Integer totalRescues = 0;
        private Double co2SavedKg = 0.0;
        private Integer treesEquivalent = 0;
        private String monthYear;

        ImpactStatsBuilder() {}

        public ImpactStatsBuilder id(Long id) { this.id = id; return this; }
        public ImpactStatsBuilder user(User user) { this.user = user; return this; }
        public ImpactStatsBuilder totalMeals(Integer totalMeals) { this.totalMeals = totalMeals; return this; }
        public ImpactStatsBuilder totalRescues(Integer totalRescues) { this.totalRescues = totalRescues; return this; }
        public ImpactStatsBuilder co2SavedKg(Double co2SavedKg) { this.co2SavedKg = co2SavedKg; return this; }
        public ImpactStatsBuilder treesEquivalent(Integer treesEquivalent) { this.treesEquivalent = treesEquivalent; return this; }
        public ImpactStatsBuilder monthYear(String monthYear) { this.monthYear = monthYear; return this; }

        public ImpactStats build() {
            return new ImpactStats(id, user, totalMeals, totalRescues, co2SavedKg, treesEquivalent, monthYear);
        }
    }
}
