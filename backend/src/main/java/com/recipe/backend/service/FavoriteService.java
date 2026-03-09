package com.recipe.backend.service;

import com.recipe.backend.model.Recipe;

import java.util.List;

public interface FavoriteService {

    void addToFavorites(Long recipeId, String email);

    void removeFromFavorites(Long recipeId, String email);

    List<Recipe> getFavorites(String email);
}
