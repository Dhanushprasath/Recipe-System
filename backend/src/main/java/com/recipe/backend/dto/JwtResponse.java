
package com.recipe.backend.dto;

public class JwtResponse {

    private String message;
    private String token;
    private String role;

    public JwtResponse(String message, String token, String role) {
        this.message = message;
        this.token = token;
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}

