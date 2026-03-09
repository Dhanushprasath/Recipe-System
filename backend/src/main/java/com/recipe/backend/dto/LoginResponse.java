package com.recipe.backend.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String username;

    // Constructor
    public LoginResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    // Optional: Setters if you need to modify after creation
    public void setToken(String token) {
        this.token = token;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
