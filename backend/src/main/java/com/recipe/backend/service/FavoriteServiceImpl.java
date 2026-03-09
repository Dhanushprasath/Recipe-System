
package com.recipe.backend.service;

import com.recipe.backend.model.Favorite;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.FavoriteRepository;
import com.recipe.backend.repository.RecipeRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    public void addToFavorites(Long recipeId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        if (favoriteRepository.existsByUserAndRecipe(user, recipe)) return;

        Favorite favorite = Favorite.builder()
                .user(user)
                .recipe(recipe)
                .build();

        favoriteRepository.save(favorite);
    }

    @Override
    public void removeFromFavorites(Long recipeId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        favoriteRepository.findByUserAndRecipe(user, recipe)
                .ifPresent(favoriteRepository::delete);
    }

    @Override
    public List<Recipe> getFavorites(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.findByUser(user)
                .stream()
                .map(Favorite::getRecipe)
                .toList();
    }
}
