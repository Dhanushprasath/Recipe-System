

package com.recipe.backend.controller;
import com.recipe.backend.dto.IngredientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.recipe.backend.dto.RecipeRequest;
import com.recipe.backend.dto.RecipeResponse;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.Role;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.RecipeRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.RecipeService;
import lombok.RequiredArgsConstructor;
//import org.json.JSONArray;
//import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestBody;


import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    @Autowired
    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    @PostMapping
    public ResponseEntity<RecipeResponse> addRecipe(
            @RequestBody RecipeRequest payload,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.createRecipe(payload, user));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RecipeResponse>> myRecipes(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(recipeService.getMyRecipes(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipe(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipe(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> editRecipe(
            @PathVariable Long id,
            @RequestBody RecipeRequest payload,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(recipeService.updateRecipe(id, payload, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        recipeService.deleteRecipe(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> listRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                recipeService.listRecipes(PageRequest.of(page, size)));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countRecipes() {
        return ResponseEntity.ok(recipeService.countRecipes());
    }

    @GetMapping("/others")
    public ResponseEntity<List<RecipeResponse>> getOtherUsersRecipes(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(recipeService.getOtherUsersRecipes(user.getEmail()));
    }

    @GetMapping("/admin/unapproved")
    public ResponseEntity<List<RecipeResponse>> getUnapproved(Authentication authentication) {
        User admin = getUserFromAuth(authentication);

        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(recipeService.getUnapprovedUserRecipes());
    }

    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<Void> approveRecipe(
            @PathVariable Long id,
            Authentication authentication) {

        User admin = getUserFromAuth(authentication);
        recipeService.approveRecipe(id, admin);
        return ResponseEntity.ok().build();
    }

    private User getUserFromAuth(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "User not found"));
    }

















        // Generate INGREDIENTS ONLY
        @PostMapping("/generate-ingredients")
        public ResponseEntity<List<IngredientResponse>> generateIngredients(
                @RequestBody Map<String,String> body) throws Exception {

            // Convert Set to List for consistent JSON
            List<IngredientResponse> ingredients = new ArrayList<>(
                    recipeService.generateIngredients(body.get("recipeName"))
            );

            return ResponseEntity.ok(ingredients);
        }


    // Generate DESCRIPTION ONLY
        @PostMapping("/generate-description")
        public ResponseEntity<String> generateDescription(
                @RequestBody Map<String,Object> body) throws Exception {

            String recipeName = (String) body.get("recipeName");
            List<String> ingredients = (List<String>) body.get("ingredients");

            return ResponseEntity.ok(
                    recipeService.generateDescription(recipeName, ingredients)
            );
        }
    }











