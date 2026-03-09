package com.recipe.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FallbackNutritionDTO {

    private String ingredientName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;
    private String unit;
}
