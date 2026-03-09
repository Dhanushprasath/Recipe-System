package com.recipe.backend.service;

import com.recipe.backend.dto.*;

import java.util.List;
import java.util.Map;

public interface AdminService {

    // -------------------- Users --------------------
    List<UserResponse> getAllUsers();
    void deleteUser(Long userId);
    void banUser(Long userId);

    // -------------------- Recipes --------------------
    List<RecipeResponse> getAllRecipes();
    void deleteRecipe(Long recipeId);
    void approveRecipe(Long recipeId);
    void rejectRecipe(Long recipeId);
    RecipeResponse addRecipeByAdmin(RecipeRequest request, String adminEmail);

//    List<RecipeResponse> getMyRecipes(String adminEmail);


    // -------------------- Comments --------------------
    List<CommentResponse> getAllComments();
    void deleteComment(Long commentId);

    // -------------------- Stats --------------------
    Map<String, Object> getSystemStats();
    // AdminService.java
    List<RecipeResponse> getMyRecipes(String email);

    List<UserWithRecipesResponse> getUsersWithRecipes();
    RecipeResponse updateRecipe(Long id, RecipeRequest request);

    // -------------------- Recipes from other users (FIXED) --------------------
    RecipeResponse getRecipeById(Long id);
    List<RecipeResponse> getUnapprovedRecipes();

    List<RecipeResponse> getRejectedRecipes();



}
