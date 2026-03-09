package com.recipe.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.backend.model.Nutrition;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.repository.NutritionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutritionRepository nutritionRepository;
    private final FallbackNutritionService fallbackService;

    @Value("${usda.api.key}")
    private String USDA_API_KEY;

    @Value("${usda.api.url:https://api.nal.usda.gov/fdc/v1/foods/search}")
    private String USDA_API_URL;

    private final WebClient client = WebClient.builder().build();

    // =========================
    // FETCH NUTRITION
    // =========================
    public Nutrition fetchNutrition(String ingredientName, Recipe recipe) {
        String name = ingredientName.toLowerCase();

        // 1️⃣ Check DB first
        var cached = nutritionRepository.findByIngredientNameIgnoreCase(name);
        if (cached.isPresent()) {
            Nutrition n = cached.get();
            n.setRecipe(recipe); // attach recipe
            return nutritionRepository.save(n);
        }

        // 2️⃣ Try USDA
        try {
            Nutrition usda = fetchAndSave(name, recipe);
            if (usda != null) return usda;
        } catch (Exception ignored) { }

        // 3️⃣ Fallback

        Nutrition fallback = fallbackService.getFallbackNutrition(name, recipe);

        if (fallback != null) {
            fallback.setIngredientName(name);
            fallback.setSource("fallback");
            return nutritionRepository.save(fallback);
        }

        // 4️⃣ Empty
        Nutrition empty = new Nutrition();
        empty.setIngredientName(name);
        empty.setCalories(0.0);
        empty.setProtein(0.0);
        empty.setCarbs(0.0);
        empty.setFats(0.0);
        empty.setUnit("g");
        empty.setRecipe(recipe);
        empty.setSource("empty");
        empty.setFromCache(true);

        return nutritionRepository.save(empty);
    }

    private Nutrition fetchAndSave(String name, Recipe recipe) {
        try {
            Nutrition real = fetchFromUSDA(name);
            if (real.getCalories() == 0.0 && real.getProtein() == 0.0 &&
                    real.getCarbs() == 0.0 && real.getFats() == 0.0) {
                return null;
            }
            real.setRecipe(recipe);
            return nutritionRepository.save(real);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch nutrition from USDA: " + name, ex);
        }
    }

    private Nutrition fetchFromUSDA(String ingredient) throws Exception {
        String url = USDA_API_URL
                + "?query=" + java.net.URLEncoder.encode(ingredient, java.nio.charset.StandardCharsets.UTF_8)
                + "&pageSize=1&api_key=" + USDA_API_KEY;

        String response = client.get().uri(url).retrieve().bodyToMono(String.class).block();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode foods = root.path("foods");
        if (!foods.isArray() || foods.isEmpty()) return emptyNutrition(ingredient);

        JsonNode nutrients = foods.get(0).path("foodNutrients");

        double calories = 0, protein = 0, fats = 0, carbs = 0;
        for (JsonNode n : nutrients) {
            String nutrientName = n.path("nutrientName").asText("").toLowerCase();
            double value = n.path("value").asDouble(0.0);
            if (nutrientName.contains("energy")) calories = value;
            else if (nutrientName.contains("protein")) protein = value;
            else if (nutrientName.contains("fat")) fats = value;
            else if (nutrientName.contains("carbohydrate")) carbs = value;
        }

        return Nutrition.builder()
                .ingredientName(ingredient)
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .fats(fats)
                .unit("g")
                .source("usda")
                .fromCache(false)
                .build();
    }

    private Nutrition emptyNutrition(String name) {
        Nutrition n = new Nutrition();
        n.setIngredientName(name);
        n.setCalories(0.0);
        n.setProtein(0.0);
        n.setCarbs(0.0);
        n.setFats(0.0);
        n.setUnit("g");
        n.setSource("empty");
        n.setFromCache(false);
        return n;
    }

    public Nutrition saveNutritionFromUSDA(Recipe recipe, Nutrition nutritionData) {
        nutritionData.setRecipe(recipe);
        nutritionData.setUnit("g");
        return nutritionRepository.save(nutritionData);
    }
    public Nutrition getNutritionForRecipe(Long recipeId) {
        Optional<Nutrition> nutritionOpt = nutritionRepository.findByRecipeId(recipeId);
        return nutritionOpt.orElse(null);
    }


    public Nutrition fetchNutritionPreview(String ingredientName) {

        String name = ingredientName.toLowerCase();

        // Try USDA
        try {
            Nutrition usda = fetchFromUSDA(name);
            if (usda != null) return usda;
        } catch (Exception ignored) {}

        // Fallback
        Nutrition fallback = fallbackService.getFallbackNutrition(name, null);


        if (fallback != null) return fallback;

        // Empty
        Nutrition empty = new Nutrition();
        empty.setIngredientName(name);
        empty.setCalories(0.0);
        empty.setProtein(0.0);
        empty.setCarbs(0.0);
        empty.setFats(0.0);
        empty.setUnit("g");

        return empty;
    }


}
