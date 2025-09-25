package com.resumeenhancer.dto;

public class AuthResponse {
    private Long userId;
    private String token;
    private String email;

    public AuthResponse() {}

    public AuthResponse(Long userId, String token, String email) {
        this.userId = userId;
        this.token = token;
        this.email = email;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
