package com.foodbridge.dto;

import com.foodbridge.entity.User.Role;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
    private String phone;
    private String city;
    private Double latitude;
    private Double longitude;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, Role role, String phone, String city, Double latitude, Double longitude) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
}
