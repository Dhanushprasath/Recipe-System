package com.recipe.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Builder

@AllArgsConstructor
public class NutritionItemResponse {
    private String name;
    private double amount;
    private String unit;
    private Long id;

    private String ingredientName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;


    public NutritionItemResponse(String name, double amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }
}


