package com.foodbridge.dto;

public class AuthResponse {
    private String token;
    private String role;
    private Long userId;

    public AuthResponse() {
    }

    public AuthResponse(String token, String role, Long userId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private String role;
        private Long userId;

        AuthResponseBuilder() {}

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder role(String role) { this.role = role; return this; }
        public AuthResponseBuilder userId(Long userId) { this.userId = userId; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, role, userId);
        }
    }
}
