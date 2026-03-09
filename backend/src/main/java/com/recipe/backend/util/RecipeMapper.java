
//    package com.recipe.backend.util;
//    import com.recipe.backend.dto.*;
//    import com.recipe.backend.model.Ingredient;
//    import com.recipe.backend.model.Recipe;
//    import org.springframework.stereotype.Component;
//
//    import java.util.HashSet;
//    import java.util.Set;
//    import java.util.stream.Collectors;
//
//@Component
//
//    public class RecipeMapper {
//
//        /* =========================
//           REQUEST → ENTITY
//        ========================== */
//        public Recipe toEntity(RecipeRequest request) {
//            return Recipe.builder()
//                    .title(request.getTitle())
//                    .description(request.getDescription())
//                    .servings(request.getServings())
//                    .cookingTime(request.getCookingTime())
//                    .build();
//        }
//
//        /* =========================
//           ENTITY → RESPONSE
//        ========================== */
//        public RecipeResponse toResponse(Recipe recipe) {
//
//            RecipeResponse dto = new RecipeResponse();
//            dto.setId(recipe.getId());
//            dto.setTitle(recipe.getTitle());
//            dto.setDescription(recipe.getDescription());
//            dto.setServings(recipe.getServings());
//            dto.setApproved(recipe.isApproved());
//            dto.setCookingTime(recipe.getCookingTime());
//            // 🔹 Recalculate totals from ingredients (null-safe)
//            double totalCalories = recipe.getIngredients() != null
//                    ? recipe.getIngredients().stream().mapToDouble(i -> i.getCalories() != null ? i.getCalories() : 0).sum()
//                    : 0;
//            double totalProtein = recipe.getIngredients() != null
//                    ? recipe.getIngredients().stream().mapToDouble(i -> i.getProtein() != null ? i.getProtein() : 0).sum()
//                    : 0;
//            double totalCarbs = recipe.getIngredients() != null
//                    ? recipe.getIngredients().stream().mapToDouble(i -> i.getCarbs() != null ? i.getCarbs() : 0).sum()
//                    : 0;
//            double totalFats = recipe.getIngredients() != null
//                    ? recipe.getIngredients().stream().mapToDouble(i -> i.getFats() != null ? i.getFats() : 0).sum()
//                    : 0;
//
//            dto.setTotalCalories(totalCalories);
//            dto.setTotalProtein(totalProtein);
//            dto.setTotalCarbs(totalCarbs);
//            dto.setTotalFats(totalFats);
//
//            dto.setCreatedAt(recipe.getCreatedAt());
//            dto.setUpdatedAt(recipe.getUpdatedAt());
//            if (recipe.getIngredients() != null) {
//                Set<IngredientResponse> ingredientResponses = recipe.getIngredients()
//                        .stream()
//                        .map(ingredient -> {
//                            IngredientResponse ir = new IngredientResponse();
//                            ir.setName(ingredient.getName());
//                            ir.setQuantity(String.valueOf(ingredient.getQuantity()));
//                            ir.setUnit(ingredient.getUnit());
//                            return ir;
//                        }).collect(Collectors.toSet());
//                dto.setIngredients(ingredientResponses);
//            }
//
//            dto.setCreatedByName(
//                    recipe.getCreatedBy() != null
//                            ? recipe.getCreatedBy().getUsername()
//                            : "Admin"
//            );
//
//            return dto;
//        }
//
//    public static RecipeResponse mapToRecipeResponse(Recipe recipe) {
//
//        // Map Ingredients
//        Set<IngredientResponse> ingredients = recipe.getIngredients() == null ? new HashSet<>()
//                : recipe.getIngredients().stream()
//                .map(ing -> IngredientResponse.builder()
//                        .id(ing.getId())
//                        .name(ing.getName())
//                        .quantity(String.valueOf(ing.getQuantity()))
//                        .unit(ing.getUnit())
//                        .calories(ing.getCalories())
//                        .protein(ing.getProtein())
//                        .carbs(ing.getCarbs())
//                        .fats(ing.getFats())
//                        .build())
//                .collect(Collectors.toSet());
//
//        // Map Nutrition
//        Set<NutritionItemResponse> nutritionItems = recipe.getNutrition() == null
//                ? new HashSet<>()
//                : recipe.getNutrition().stream()
//                .map(nutrition -> NutritionItemResponse.builder()
//                        .id(nutrition.getId())
//                        .name(nutrition.getName())
//                        .ingredientName(nutrition.getIngredientName())
//                        .calories(nutrition.getCalories())
//                        .protein(nutrition.getProtein())
//                        .carbs(nutrition.getCarbs())
//                        .fats(nutrition.getFats())
//                        .unit(nutrition.getUnit())
//                        .build())
//                .collect(Collectors.toSet());
//
//        // Calculate totals
//        double totalCalories = nutritionItems.stream()
//                .mapToDouble(n -> n.getCalories() != null ? n.getCalories() : 0)
//                .sum();
//        double totalProtein = nutritionItems.stream()
//                .mapToDouble(n -> n.getProtein() != null ? n.getProtein() : 0)
//                .sum();
//        double totalCarbs = nutritionItems.stream()
//                .mapToDouble(n -> n.getCarbs() != null ? n.getCarbs() : 0)
//                .sum();
//        double totalFats = nutritionItems.stream()
//                .mapToDouble(n -> n.getFats() != null ? n.getFats() : 0)
//                .sum();
//
//        return RecipeResponse.builder()
//                .id(recipe.getId())
//                .title(recipe.getTitle())
//                .description(recipe.getDescription())
//                .servings(recipe.getServings())
//                .cookingTime(recipe.getCookingTime())
//                .approved(recipe.isApproved())
//                .totalCalories(totalCalories)
//                .totalProtein(totalProtein)
//                .totalCarbs(totalCarbs)
//                .totalFats(totalFats)
//                .createdById(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null)
//                .createdByUsername(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin")
//                .createdByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : null)
//                .ingredients(ingredients)
//                // pass the set of NutritionItemResponse
//                .createdAt(recipe.getCreatedAt())
//                .updatedAt(recipe.getUpdatedAt())
//                .build();
//    }
//
//
//    public static Ingredient mapToIngredient(IngredientRequest request) {
//        Ingredient ingredient = new Ingredient();
//        ingredient.setName(request.getName());
//        ingredient.setQuantity(request.getQuantity());
//        ingredient.setUnit(request.getUnit());
//        ingredient.setCalories(request.getCalories() != null ? request.getCalories() : 0);
//        ingredient.setProtein(request.getProtein() != null ? request.getProtein() : 0);
//        ingredient.setCarbs(request.getCarbs() != null ? request.getCarbs() : 0);
//        ingredient.setFats(request.getFats() != null ? request.getFats() : 0);
//        return ingredient;
//    }
//
//}
//




package com.recipe.backend.util;

import com.recipe.backend.dto.*;
import com.recipe.backend.model.Ingredient;
import com.recipe.backend.model.Recipe;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    /* =========================
       REQUEST → ENTITY
    ========================== */
    public Recipe toEntity(RecipeRequest request) {
        return Recipe.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .servings(request.getServings())
                .cookingTime(request.getCookingTime())
                .build();
    }

    /* =========================
       ENTITY → RESPONSE
    ========================== */
//    public RecipeResponse toResponse(Recipe recipe) {
//
//        RecipeResponse dto = new RecipeResponse();
//        dto.setId(recipe.getId());
//        dto.setTitle(recipe.getTitle());
//        dto.setDescription(recipe.getDescription());
//        dto.setServings(recipe.getServings());
//        dto.setApproved(recipe.getApproved());// instead of recipe.isApproved()
//
//        dto.setCookingTime(recipe.getCookingTime());
//        dto.setCreatedAt(recipe.getCreatedAt());
//        dto.setUpdatedAt(recipe.getUpdatedAt());
//
//        // Map Ingredients
//        Set<IngredientResponse> ingredientResponses = recipe.getIngredients() == null ? new HashSet<>()
//                : recipe.getIngredients().stream()
//                .map(ing -> IngredientResponse.builder()
//                        .id(ing.getId())
//                        .name(ing.getName())
//                        .quantity(String.valueOf(ing.getQuantity()))
//                        .unit(ing.getUnit())
//                        .calories(ing.getCalories())
//                        .protein(ing.getProtein())
//                        .carbs(ing.getCarbs())
//                        .fats(ing.getFats())
//                        .build())
//                .collect(Collectors.toSet());
//
//        dto.setIngredients(ingredientResponses);
//
//        dto.setTotalCalories(recipe.getTotalCalories());
//        dto.setTotalProtein(recipe.getTotalProtein());
//        dto.setTotalCarbs(recipe.getTotalCarbs());
//        dto.setTotalFats(recipe.getTotalFats());
//
//
//
//        // CreatedBy info
//        dto.setCreatedById(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null);
//        dto.setCreatedByUsername(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin");
//        dto.setCreatedByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : null);
//        dto.setCreatedByName(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin");
//
//        return dto;
//    }
    public RecipeResponse toResponse(Recipe recipe) {
        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .servings(recipe.getServings())
                .cookingTime(recipe.getCookingTime())
                .totalCalories(recipe.getTotalCalories())
                .totalProtein(recipe.getTotalProtein())
                .totalCarbs(recipe.getTotalCarbs())
                .totalFats(recipe.getTotalFats())
                .approved(recipe.getApproved() != null ? recipe.getApproved() : false)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(
                        recipe.getIngredients() != null
                                ? recipe.getIngredients().stream()
                                .map(ing -> IngredientResponse.builder()
                                        .id(ing.getId())
                                        .name(ing.getName())
                                        .quantity(Double.valueOf(String.valueOf(ing.getQuantity())))
                                        .unit(ing.getUnit())
                                        .calories(ing.getCalories() != null ? ing.getCalories() : 0.0)
                                        .protein(ing.getProtein() != null ? ing.getProtein() : 0.0)
                                        .carbs(ing.getCarbs() != null ? ing.getCarbs() : 0.0)
                                        .fats(ing.getFats() != null ? ing.getFats() : 0.0)
                                        .build())
                                .collect(Collectors.toSet())
                                : new HashSet<>()
                )
                .createdById(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null)
                .createdByUsername(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin")
                .createdByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : null)
                .createdByName(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin")
                .build();
    }

    /* =========================
       IngredientRequest → Ingredient
    ========================== */
    public static Ingredient mapToIngredient(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setQuantity(request.getQuantity());
        ingredient.setUnit(request.getUnit());
        ingredient.setCalories(request.getCalories() != null ? request.getCalories() : 0);
        ingredient.setProtein(request.getProtein() != null ? request.getProtein() : 0);
        ingredient.setCarbs(request.getCarbs() != null ? request.getCarbs() : 0);
        ingredient.setFats(request.getFats() != null ? request.getFats() : 0);

        return ingredient;
    }

    public static RecipeResponse mapToRecipeResponse(Recipe recipe) {

        // Map Ingredients
        Set<IngredientResponse> ingredients = recipe.getIngredients() == null ? new HashSet<>()
                : recipe.getIngredients().stream()
                .map(ing -> IngredientResponse.builder()
                        .id(ing.getId())
                        .name(ing.getName())
                        .quantity(Double.valueOf(String.valueOf(ing.getQuantity())))
                        .unit(ing.getUnit())
                        .calories(ing.getCalories())
                        .protein(ing.getProtein())
                        .carbs(ing.getCarbs())
                        .fats(ing.getFats())
                        .build())
                .collect(Collectors.toSet());

        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .servings(recipe.getServings())
                .cookingTime(recipe.getCookingTime())
                .approved(recipe.getApproved()) // instead of recipe.isApproved()

                .totalCalories(recipe.getTotalCalories())
                .totalProtein(recipe.getTotalProtein())
                .totalCarbs(recipe.getTotalCarbs())
                .totalFats(recipe.getTotalFats())
                .createdById(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null)
                .createdByUsername(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Unknown")
                .createdByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : "Unknown")
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(ingredients)
                .build();
    }
}
