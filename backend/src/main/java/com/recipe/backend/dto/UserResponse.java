package com.recipe.backend.dto;

import com.recipe.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private long recipeCount;
    private boolean enabled;

}
