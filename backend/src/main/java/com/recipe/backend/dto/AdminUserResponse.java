
package com.recipe.backend.dto;

import java.util.List;

public record AdminUserResponse<AdminRecipeResponse>(
        Long id,
        String username,
        String email,
        List<AdminRecipeResponse> recipes
) {}

