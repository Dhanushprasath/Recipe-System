package com.recipe.backend.controller;

import com.recipe.backend.model.Nutrition;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.repository.NutritionRepository;
import com.recipe.backend.service.FallbackNutritionService;
import com.recipe.backend.service.NutritionService;
import com.recipe.backend.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class NutritionController {

    private final NutritionService nutritionService;
    private final FallbackNutritionService fallbackNutritionService;
    private final RecipeService recipeService;
    private final NutritionRepository nutritionRepository;

    // ================= POST: Fetch nutrition from ingredients =================

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchNutrition(@RequestBody Map<String, Object> payload) {
        try {
            Object obj = payload.get("ingredients");
            if (!(obj instanceof List)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ingredients must be a list"));
            }

            List<?> ingListRaw = (List<?>) obj;
            double calories = 0, protein = 0, carbs = 0, fat = 0;

            for (Object itemObj : ingListRaw) {
                if (!(itemObj instanceof Map)) continue;
                Map<String, Object> item = (Map<String, Object>) itemObj;

                String name = ((String) item.get("name")).trim();
                double quantity = ((Number) item.get("quantity")).doubleValue();
                String unit = ((String) item.get("unit")).trim();

                double qtyInGrams = convertToGrams(quantity, unit);

//                Nutrition n;
//                try {
//                    n = nutritionService.fetchNutritionPreview(name); // preview only
//                } catch (Exception ex) {
//                    n = fallbackNutritionService.getFallbackNutrition(name, null); // recipe=null
//                }

                Nutrition n;

                // FIRST: try local fallback JSON
                n = fallbackNutritionService.getFallbackNutrition(name, null);

                if (n != null && n.getCalories() != null && n.getCalories() > 0) {
                    System.out.println("✅ FALLBACK USED: " + name);
                } else {
                    // SECOND: USDA
                    System.out.println("🌐 USDA USED: " + name);
                    n = nutritionService.fetchNutritionPreview(name);
                }


                double factor = 1.0;
                String nutritionUnit = (n.getUnit() == null || n.getUnit().isBlank()) ? "g" : n.getUnit().toLowerCase();
                switch (nutritionUnit) {
                    case "g":
                    case "ml":
                        factor = qtyInGrams / 100.0;
                        break;
                    case "tsp":
                    case "tbsp":
                        factor = qtyInGrams / 1.0;
                        break;
                    default:
                        factor = qtyInGrams / 100.0;
                }

                calories += n.getCalories() * factor;
                protein += n.getProtein() * factor;
                carbs += n.getCarbs() * factor;
                fat += n.getFats() * factor;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("calories", Math.round(calories));
            response.put("protein", protein);
            response.put("carbs", carbs);
            response.put("fat", fat);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/save/{recipeId}")
    public ResponseEntity<?> saveNutrition(@PathVariable Long recipeId, @RequestBody Map<String, Object> payload) {
        try {
            Recipe recipe = recipeService.getRecipeById(recipeId);
            if (recipe == null) return ResponseEntity.badRequest().body(Map.of("error", "Recipe not found"));

            Object obj = payload.get("nutrition");
            if (!(obj instanceof List)) return ResponseEntity.badRequest().body(Map.of("error", "Nutrition must be a list"));

            List<?> nutritionList = (List<?>) obj;

            for (Object nObj : nutritionList) {
                if (!(nObj instanceof Map)) continue;
                Map<String, Object> nMap = (Map<String, Object>) nObj;

                Nutrition n = new Nutrition();
                n.setRecipe(recipe);
                n.setCalories(((Number) nMap.get("calories")).doubleValue());
                n.setProtein(((Number) nMap.get("protein")).doubleValue());
                n.setCarbs(((Number) nMap.get("carbs")).doubleValue());
                n.setFats(((Number) nMap.get("fats")).doubleValue());
                    
                n.setUnit("g");

                nutritionRepository.save(n);
            }

            return ResponseEntity.ok(Map.of("message", "Nutrition saved successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



    // ================= GET: Fetch nutrition by recipeId =================
    @GetMapping("/fetch-by-recipe")
    public ResponseEntity<Nutrition> fetchNutritionByRecipe(@RequestParam Long recipeId) {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) return ResponseEntity.badRequest().build();

        Nutrition nutrition = nutritionService.getNutritionForRecipe(recipeId);
        if (nutrition == null) {
            nutrition = fallbackNutritionService.getFallbackNutrition("default", recipe);
        }

        return ResponseEntity.ok(nutrition);
    }

    // ================= GET: Fallback endpoint =================
    @PostMapping ("/fallback")
    public Nutrition fallbackNutrition() {
        Nutrition fallback = new Nutrition();
        fallback.setCalories(0.0);
        fallback.setProtein(0.0);
        fallback.setCarbs(0.0);
        fallback.setFats(0.0);
        fallback.setUnit("g");
        return fallback;
    }

    // ================= Helper: Unit conversion =================
    private double convertToGrams(double quantity, String unit) {
        switch (unit.toLowerCase()) {
            case "g": return quantity;
            case "kg": return quantity * 1000;
            case "mg": return quantity / 1000;
            case "lb": return quantity * 453.6;
            case "oz": return quantity * 28.35;
            case "tsp": return quantity * 5;
            case "tbsp": return quantity * 15;
            case "ml": return quantity;
            default: return quantity;
        }
    }
}
