package com.recipe.backend.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer servings;
    private Integer cookingTime;

    private boolean approved;

    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFats;

    private Long createdById;
    private String createdByUsername;
    private String createdByEmail;

    @Builder.Default
    private Set<IngredientResponse> ingredients = new HashSet<>();




    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String createdByName;
}
