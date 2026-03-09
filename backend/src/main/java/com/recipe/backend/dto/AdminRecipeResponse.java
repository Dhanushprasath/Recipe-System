package com.recipe.backend.dto;

public record AdminRecipeResponse(
        Long id,
        String title,
        boolean approved
) {}

