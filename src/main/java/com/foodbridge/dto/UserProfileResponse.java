package com.foodbridge.dto;

import com.foodbridge.entity.User.Role;
import com.foodbridge.entity.User.UserStatus;
import com.foodbridge.entity.VerificationStatus;
import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String city;
    private Double latitude;
    private Double longitude;
    private UserStatus status;
    private Double trustScore;
    private LocalDateTime createdAt;
    private VerificationStatus verificationStatus;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String name, String email, Role role, String phone, String city,
                              Double latitude, Double longitude, UserStatus status, Double trustScore, LocalDateTime createdAt,
                              VerificationStatus verificationStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.trustScore = trustScore;
        this.createdAt = createdAt;
        this.verificationStatus = verificationStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Double getTrustScore() { return trustScore; }
    public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public static UserProfileResponseBuilder builder() {
        return new UserProfileResponseBuilder();
    }

    public static class UserProfileResponseBuilder {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private String phone;
        private String city;
        private Double latitude;
        private Double longitude;
        private UserStatus status;
        private Double trustScore;
        private LocalDateTime createdAt;
        private VerificationStatus verificationStatus;

        UserProfileResponseBuilder() {}

        public UserProfileResponseBuilder id(Long id) { this.id = id; return this; }
        public UserProfileResponseBuilder name(String name) { this.name = name; return this; }
        public UserProfileResponseBuilder email(String email) { this.email = email; return this; }
        public UserProfileResponseBuilder role(Role role) { this.role = role; return this; }
        public UserProfileResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public UserProfileResponseBuilder city(String city) { this.city = city; return this; }
        public UserProfileResponseBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public UserProfileResponseBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public UserProfileResponseBuilder status(UserStatus status) { this.status = status; return this; }
        public UserProfileResponseBuilder trustScore(Double trustScore) { this.trustScore = trustScore; return this; }
        public UserProfileResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserProfileResponseBuilder verificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }

        public UserProfileResponse build() {
            return new UserProfileResponse(id, name, email, role, phone, city, latitude, longitude, status, trustScore, createdAt, verificationStatus);
        }
    }
}
