package com.recipe.backend.controller;

import com.recipe.backend.dto.RecipeResponse;

import com.recipe.backend.service.FavoriteService;
import com.recipe.backend.util.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final RecipeMapper recipeMapper;

    // ❤️ ADD
    @PostMapping("/recipes/favorite/{id}")
    public ResponseEntity<Void> addToFavorites(
            @PathVariable Long id,
            Authentication auth) {

        favoriteService.addToFavorites(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    // 💔 REMOVE
    @DeleteMapping("/recipes/favorite/{id}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long id,
            Authentication auth) {

        favoriteService.removeFromFavorites(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    // 📥 GET FAVORITES
    @GetMapping("/recipes/favorites")
    public ResponseEntity<List<RecipeResponse>> getFavorites(
            Authentication auth) {

        return ResponseEntity.ok(
                favoriteService.getFavorites(auth.getName())
                        .stream()
                        .map(recipeMapper::toResponse)
                        .toList()
        );
    }
}
