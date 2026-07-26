package com.foodbridge.entity;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;
    private String city;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, columnDefinition = "double default 5.0")
    private Double trustScore = 5.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private String verificationDocumentUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String name, String email, String passwordHash, Role role, String phone, String city,
                Double latitude, Double longitude, UserStatus status, Double trustScore, LocalDateTime createdAt,
                VerificationStatus verificationStatus, String verificationDocumentUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.phone = phone;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.trustScore = trustScore;
        this.createdAt = createdAt;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.PENDING;
        this.verificationDocumentUrl = verificationDocumentUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
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
    public String getVerificationDocumentUrl() { return verificationDocumentUrl; }
    public void setVerificationDocumentUrl(String verificationDocumentUrl) { this.verificationDocumentUrl = verificationDocumentUrl; }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String passwordHash;
        private Role role;
        private String phone;
        private String city;
        private Double latitude;
        private Double longitude;
        private UserStatus status;
        private Double trustScore = 5.0;
        private LocalDateTime createdAt;
        private VerificationStatus verificationStatus = VerificationStatus.PENDING;
        private String verificationDocumentUrl;

        UserBuilder() {}

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder city(String city) { this.city = city; return this; }
        public UserBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public UserBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public UserBuilder status(UserStatus status) { this.status = status; return this; }
        public UserBuilder trustScore(Double trustScore) { this.trustScore = trustScore; return this; }
        public UserBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserBuilder verificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public UserBuilder verificationDocumentUrl(String verificationDocumentUrl) { this.verificationDocumentUrl = verificationDocumentUrl; return this; }

        public User build() {
            return new User(id, name, email, passwordHash, role, phone, city, latitude, longitude, status, trustScore, createdAt, verificationStatus, verificationDocumentUrl);
        }
    }

    public enum Role {
        DONOR, NGO, ADMIN, VOLUNTEER
    }

    public enum UserStatus {
        ACTIVE, PENDING, BLOCKED
    }
}
