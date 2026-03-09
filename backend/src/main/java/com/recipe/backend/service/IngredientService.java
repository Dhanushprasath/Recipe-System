package com.recipe.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.backend.dto.FallbackNutritionDTO;
import com.recipe.backend.model.Ingredient;
import com.recipe.backend.model.Nutrition;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.repository.NutritionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final NutritionService nutritionService;


    private final FallbackNutritionService fallbackService;

    private final Map<String, FallbackNutritionDTO> fallbackMap = new HashMap<>();

    // =============================
    // LOAD FALLBACK JSON
    // =============================
    @PostConstruct
    public void loadFallbackJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<FallbackNutritionDTO> list = mapper.readValue(
                    new ClassPathResource("nutrition_fallback.json").getInputStream(),
                    new TypeReference<List<FallbackNutritionDTO>>() {}
            );

            for (FallbackNutritionDTO n : list) {
                fallbackMap.put(normalize(n.getIngredientName()), n);
            }

            System.out.println("✅ Fallback loaded: " + fallbackMap.keySet());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load fallback nutrition JSON", e);
        }
    }

    // =============================
    // MAIN RESOLVER
    // =============================
    public Ingredient resolveIngredientNutrition(String name, double quantity, String unit, Recipe recipe) {

        String canonical = name.trim().toLowerCase();

        Nutrition nut;
        try {
            nut = nutritionService.fetchNutrition(canonical,recipe); // USDA or cached
        } catch (Exception e) {
            nut = null;
        }

        if (nut == null) {
            nut = new Nutrition();
            nut.setCalories(0.0);
            nut.setProtein(0.0);
            nut.setCarbs(0.0);
            nut.setFats(0.0);
        }

        double scale = quantity / 100.0;

        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setQuantity(quantity);
        ingredient.setUnit(unit);

        ingredient.setCalories(nut.getCalories() * scale);
        ingredient.setProtein(nut.getProtein() * scale);
        ingredient.setCarbs(nut.getCarbs() * scale);
        ingredient.setFats(nut.getFats() * scale);

        return ingredient; // ✅ ONLY THIS
    }

    // =============================
    // HELPERS
    // =============================
    private Nutrition mapDtoToNutrition(FallbackNutritionDTO dto) {
        Nutrition n = new Nutrition();
        n.setIngredientName(dto.getIngredientName());
        n.setCalories(dto.getCalories());
        n.setProtein(dto.getProtein());
        n.setCarbs(dto.getCarbs());
        n.setFats(dto.getFats());
        n.setSource("fallback");
        n.setFromCache(true);
        n.setUnit("g");
        return n;
    }

    private Nutrition zeroNutrition(String name) {
        Nutrition n = new Nutrition();
        n.setIngredientName(name);
        n.setCalories(0.0);
        n.setProtein(0.0);
        n.setCarbs(0.0);
        n.setFats(0.0);
        n.setSource("empty");
        n.setFromCache(true);
        n.setUnit("g");
        return n;
    }

    private String normalize(String value) {
        return value == null ? "" :
                value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static double roundSafe(Double v) {
        return v == null ? 0.0 : Math.round(v * 100.0) / 100.0;
    }
}
