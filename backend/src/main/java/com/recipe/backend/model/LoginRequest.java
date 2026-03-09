
package com.recipe.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginRequest {
    // Getters and Setters
    @Setter
    private String email;
    private String password;

}
