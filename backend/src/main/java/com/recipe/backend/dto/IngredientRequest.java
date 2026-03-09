package com.recipe.backend.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IngredientRequest {
    private String name;
    private Double quantity;
    private String unit;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;

}
