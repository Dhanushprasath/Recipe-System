    package com.recipe.backend.dto;

    import lombok.Data;

    @Data
    public class NutritionRequest {
        private String name;
        private Double calories;
        private Double protein;
        private Double carbs;
        private Double fats;
    }
