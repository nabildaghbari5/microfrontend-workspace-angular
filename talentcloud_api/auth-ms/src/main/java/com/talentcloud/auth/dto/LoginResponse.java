package com.talentcloud.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class LoginResponse {
    private String token;
    private UserResponse user;

    // Default constructor
    public LoginResponse() {
    }

    // All-args constructor
    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}