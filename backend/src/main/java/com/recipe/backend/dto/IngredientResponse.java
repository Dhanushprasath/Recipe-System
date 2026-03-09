package com.recipe.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResponse {
    private Long id;
    private String name;
    private Double quantity;
    private String unit;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;
}
