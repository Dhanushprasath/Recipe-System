package com.recipe.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecipeRequest {

    private String title;
    private String description;
    private Integer cookingTime;
    private Integer servings;

    private List<IngredientRequest> ingredients;


    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFats;
}
