package com.recipe.backend.controller;

import com.recipe.backend.dto.*;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.RecipeRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.AdminService;

import com.recipe.backend.service.RecipeService;
import com.recipe.backend.util.RecipeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.*;
import java.util.stream.Collectors;
import com.recipe.backend.util.RecipeMapper;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Panel", description = "Admin-only operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RecipeService recipeService;
    private final AdminService adminService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;



    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ==============================
    // USERS MANAGEMENT
    // ==============================
    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/users/{userId}/ban")
    @Operation(summary = "Ban / Disable a user")
    public ResponseEntity<String> banUser(@PathVariable Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok("User banned successfully");
    }

    // ==============================
    // RECIPE MANAGEMENT
    // ==============================
    @PostMapping("/recipes")
    @Operation(summary = "Admin add recipe")
    public ResponseEntity<RecipeResponse> addRecipeByAdmin(
            @Valid @RequestBody RecipeRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminService.addRecipeByAdmin(request, adminEmail));
    }

    @GetMapping("/recipes")
    @Operation(summary = "Get all recipes")
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        return ResponseEntity.ok(adminService.getAllRecipes());
    }

    @DeleteMapping("/recipes/{id}")
    @Operation(summary = "Delete a recipe by admin")
    public ResponseEntity<?> deleteRecipe(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            User requester = getUserFromAuth(authentication);
            recipeService.deleteRecipe(id, requester);
            return ResponseEntity.ok(Map.of("message", "Recipe deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/recipes/{recipeId}/approve")
    @Operation(summary = "Approve recipe")
    public ResponseEntity<String> approveRecipe(@PathVariable Long recipeId) {
        adminService.approveRecipe(recipeId);
        return ResponseEntity.ok("Recipe approved");
    }



    // ==============================
    // GET MY RECIPES
    // ==============================
    @GetMapping("/my-recipes")
    @Operation(summary = "Get recipes created by logged-in admin")
    public ResponseEntity<List<RecipeResponse>> getMyRecipes(Authentication authentication) {
        String adminEmail = authentication.getName();
        List<RecipeResponse> recipes = adminService.getMyRecipes(adminEmail);
        return ResponseEntity.ok(recipes);
    }

    // ==============================
    // GET OTHER USERS' RECIPES
    // ==============================
    @GetMapping("/recipes/other")
    @Operation(summary = "Get recipes created by other users")
    public ResponseEntity<List<RecipeResponse>> getOtherRecipes(Authentication authentication) {
        User currentUser = getUserFromAuth(authentication);

        List<Recipe> recipes = recipeRepository.findByApprovedTrueAndCreatedBy_IdNot(currentUser.getId());

        List<RecipeResponse> response = recipes.stream()
                .map(this::mapToRecipeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==============================
    // GET RECIPE BY ID
    // ==============================
    @GetMapping("/recipes/{id:[0-9]+}")
    @Operation(summary = "Get recipe by ID")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        try {
            RecipeResponse recipe = adminService.getRecipeById(id);
            if (recipe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Recipe not found"));
            }
            return ResponseEntity.ok(recipe);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Recipe not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Something went wrong: " + e.getMessage()));
        }
    }

    @PutMapping("/recipes/{id:[0-9]+}")
    @Operation(summary = "Update recipe by ID")
    public ResponseEntity<?> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        try {
            RecipeResponse updated = adminService.updateRecipe(id, request);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Recipe not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    // ==============================
    // COMMENTS
    // ==============================
    @GetMapping("/comments")
    @Operation(summary = "Get all comments")
    public ResponseEntity<List<CommentResponse>> getAllComments() {
        return ResponseEntity.ok(adminService.getAllComments());
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    // ==============================
    // USERS WITH RECIPES
    // ==============================
    @GetMapping("/users-with-recipes")
    @Operation(summary = "Get users with their recipes")
    public ResponseEntity<List<UserWithRecipesResponse>> getUsersWithRecipes() {
        return ResponseEntity.ok(adminService.getUsersWithRecipes());
    }

    // ==============================
    // SYSTEM STATS
    // ==============================
    @GetMapping("/stats")
    @Operation(summary = "View system statistics")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    // ==============================
    // UNAPPROVED RECIPES
    // ==============================
    @GetMapping("/recipes/unapproved")
    public ResponseEntity<List<RecipeResponse>> getUnapprovedRecipes() {
        return ResponseEntity.ok(recipeService.getUnapprovedUserRecipes());
    }

    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<Void> approveRecipe(@PathVariable Long id, Authentication auth) {
        User admin = getUserFromAuth(auth);
        recipeService.approveRecipe(id, admin);
        return ResponseEntity.ok().build();
    }


    // ==============================
    // HELPER: Map Recipe -> RecipeResponse
    // ==============================
    private RecipeResponse mapToRecipeResponse(Recipe recipe) {
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
                .ingredients(new HashSet<>()) // map ingredients if needed
                .build();
    }
    @GetMapping("/recipes/view/{id}")
    public ResponseEntity<Recipe> viewRecipeByAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));


    }

    @GetMapping("/users/{userId}/recipes")
    public ResponseEntity<List<RecipeResponse>> getRecipesByUser(@PathVariable Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RecipeResponse> response = recipeRepository
                .findByCreatedBy_IdAndApprovedTrue(userId)
                .stream()
                .map(RecipeMapper::mapToRecipeResponse) // STATIC
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/metrics")
//    public Map<String, Long> getDashboardMetrics() {
//
//        Map<String, Long> map = new HashMap<>();
//
//        map.put("totalRecipes", recipeRepository.count());
//        map.put("totalUsers", userRepository.count());
//
//        map.put("approvedRecipes", recipeRepository.countByApproved(true));
//        map.put("rejectedRecipes", recipeRepository.countByApproved(false));
//        map.put("pendingRecipes", recipeRepository.countByApproved(null));
//
//
//        return map;
//    }
@GetMapping("/metrics")
public Map<String, Long> getDashboardMetrics() {

    Map<String, Long> map = new HashMap<>();

    map.put("totalRecipes", recipeRepository.count());
    map.put("totalUsers", userRepository.count());

    map.put("approvedRecipes", recipeRepository.countByApproved(true));
    map.put("rejectedRecipes", (long) recipeRepository.findByRejectedTrue().size());

    // REAL pending count
    map.put("pendingRecipes", recipeRepository.countByApprovedFalseAndRejectedFalse());

    return map;
}



    // AdminController.java
    @PutMapping("/recipes/{id}/reject")
    public ResponseEntity<?> rejectRecipe(@PathVariable Long id) {
        adminService.rejectRecipe(id);
        return ResponseEntity.ok(Map.of("message", "Recipe rejected successfully"));
    }

    @GetMapping("/recipes/rejected")
    public List<RecipeResponse> getRejectedRecipes() {
        return adminService.getRejectedRecipes();
    }















}
