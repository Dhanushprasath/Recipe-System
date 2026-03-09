package com.recipe.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.backend.model.Nutrition;
import com.recipe.backend.model.Recipe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class FallbackNutritionService {

    private final Map<String, Nutrition> fallbackMap = new HashMap<>();

    @PostConstruct
    public void loadFallbackData() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream("nutrition_fallback.json");

            if (is == null) {
                throw new RuntimeException("nutrition_fallback.json NOT FOUND");
            }

            Nutrition[] list = mapper.readValue(is, Nutrition[].class);

            for (Nutrition n : list) {
                n.setSource("fallback");
                n.setFromCache(true);
                fallbackMap.put(n.getIngredientName().toLowerCase(), n);
            }

            System.out.println("✅ Loaded fallback nutrition JSON");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load fallback nutrition JSON", e);
        }
    }


    public Nutrition getFallbackNutrition(String name, Recipe recipe) {
        Nutrition n = fallbackMap.get(name.trim().toLowerCase());

        if (n == null) {
            return null;

    }
    n.setRecipe(recipe);
    return n;
}



}

