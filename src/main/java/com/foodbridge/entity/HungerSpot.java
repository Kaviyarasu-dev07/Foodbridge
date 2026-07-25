package com.foodbridge.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "hunger_spots")
public class HungerSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false)
    private String locationName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotStatus status;

    private Integer peopleCount;
    private LocalDateTime verifiedAt;

    public HungerSpot() {
    }

    public HungerSpot(Long id, User reportedBy, String locationName, Double latitude, Double longitude, SpotStatus status, Integer peopleCount, LocalDateTime verifiedAt) {
        this.id = id;
        this.reportedBy = reportedBy;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.peopleCount = peopleCount;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public SpotStatus getStatus() { return status; }
    public void setStatus(SpotStatus status) { this.status = status; }
    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public static HungerSpotBuilder builder() {
        return new HungerSpotBuilder();
    }

    public static class HungerSpotBuilder {
        private Long id;
        private User reportedBy;
        private String locationName;
        private Double latitude;
        private Double longitude;
        private SpotStatus status;
        private Integer peopleCount;
        private LocalDateTime verifiedAt;

        HungerSpotBuilder() {}

        public HungerSpotBuilder id(Long id) { this.id = id; return this; }
        public HungerSpotBuilder reportedBy(User reportedBy) { this.reportedBy = reportedBy; return this; }
        public HungerSpotBuilder locationName(String locationName) { this.locationName = locationName; return this; }
        public HungerSpotBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public HungerSpotBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public HungerSpotBuilder status(SpotStatus status) { this.status = status; return this; }
        public HungerSpotBuilder peopleCount(Integer peopleCount) { this.peopleCount = peopleCount; return this; }
        public HungerSpotBuilder verifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; return this; }

        public HungerSpot build() {
            return new HungerSpot(id, reportedBy, locationName, latitude, longitude, status, peopleCount, verifiedAt);
        }
    }

    public enum SpotStatus {
        PENDING, VERIFIED, REJECTED
    }
}
