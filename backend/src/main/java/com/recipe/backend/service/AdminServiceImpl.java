package com.recipe.backend.service;

import com.recipe.backend.dto.*;
import com.recipe.backend.model.*;
import com.recipe.backend.repository.*;
import com.recipe.backend.util.MapperUtil;
import com.recipe.backend.util.RecipeMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RecipeService recipeService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final IngredientService ingredientService;


    // -------------------- Users --------------------
//    @Override
//    public List<UserResponse> getAllUsers() {
//        return MapperUtil.toUserResponseList(userRepository.findAll());
//    }
    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .enabled(user.isEnabled())
                        // 🔥 REAL DYNAMIC COUNT
                        .recipeCount(recipeRepository.countByCreatedBy_IdAndApprovedTrue(user.getId()))
                        .build()
                )
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void banUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }

    // -------------------- Recipes --------------------
    @Override
    public List<RecipeResponse> getAllRecipes() {
        return MapperUtil.toRecipeResponseList(recipeRepository.findAll());
    }

    @Override
    public void deleteRecipe(Long recipeId) {
        recipeRepository.deleteById(recipeId);
    }

    @Override
    public void approveRecipe(Long recipeId) {
        recipeRepository.findById(recipeId).ifPresent(recipe -> {
            recipe.setApproved(true);
            recipeRepository.save(recipe);
        });
    }



    // -------------------- Comments --------------------
    @Override
    public List<CommentResponse> getAllComments() {

        // Get admin user (choose ONE approach)
        User admin = userRepository.findByRole(Role.ADMIN)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return MapperUtil.toCommentResponseList(
                commentRepository.findAll(),
                admin
        );
    }


    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }

    // -------------------- Stats --------------------
    @Override
    public Map<String, Object> getSystemStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalRecipes", recipeRepository.count(),
                "totalComments", commentRepository.count()
        );
    }

    // -------------------- Add Recipe By Admin --------------------
    @Override
    public RecipeResponse addRecipeByAdmin(RecipeRequest request, String adminEmail) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Recipe recipe = new Recipe();
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setServings(request.getServings());
        recipe.setApproved(true);
        recipe.setCreatedBy(admin);
        recipe.setCreatedAt(LocalDateTime.now());

        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        if (request.getIngredients() != null) {
            for (IngredientRequest ir : request.getIngredients()) {

                Ingredient ingredient = new Ingredient();
                ingredient.setName(ir.getName());
                ingredient.setQuantity(ir.getQuantity());
                ingredient.setUnit(ir.getUnit());

                ingredient.setCalories(ir.getCalories());
                ingredient.setProtein(ir.getProtein());
                ingredient.setCarbs(ir.getCarbs());
                ingredient.setFats(ir.getFats());

                ingredient.setRecipe(recipe);
                recipe.getIngredients().add(ingredient);

                totalCalories += ir.getCalories() != null ? ir.getCalories() : 0;
                totalProtein += ir.getProtein() != null ? ir.getProtein() : 0;
                totalCarbs += ir.getCarbs() != null ? ir.getCarbs() : 0;
                totalFats += ir.getFats() != null ? ir.getFats() : 0;
            }
        }

        recipe.setTotalCalories(totalCalories);
        recipe.setTotalProtein(totalProtein);
        recipe.setTotalCarbs(totalCarbs);
        recipe.setTotalFats(totalFats);

        Recipe saved = recipeRepository.save(recipe);
        return MapperUtil.toRecipeResponse(saved);
    }

//    // -------------------- Get My Recipes --------------------

@Override

public List<RecipeResponse> getMyRecipes(String adminEmail) {

    List<Recipe> recipes =
            recipeRepository.findAdminRecipesWithNutrition(adminEmail);

    return recipes.stream()
            .map(RecipeMapper::mapToRecipeResponse)
            .collect(Collectors.toList());
}


    // -------------------- Users with Recipes --------------------
    @Override
    public List<UserWithRecipesResponse> getUsersWithRecipes() {

        List<User> users = userRepository.findByRole(Role.USER);

        return users.stream().map(user -> {
            List<RecipeResponse> recipes = recipeRepository.findByCreatedBy(user)
                    .stream()
                    .map(recipeMapper::toResponse)
                    .toList();

            return new UserWithRecipesResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    recipes
            );
        }).toList();
    }

    // -------------------- Update Recipe --------------------
//    @Override
//    public RecipeResponse updateRecipe(Long id, RecipeRequest request) {
//
//        Recipe recipe = recipeRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Recipe not found"));
//
//        if (request.getTitle() != null)
//            recipe.setTitle(request.getTitle());
//
//        if (request.getDescription() != null)
//            recipe.setDescription(request.getDescription());
//
//        if (request.getCookingTime() != null)
//            recipe.setCookingTime(request.getCookingTime());
//
//        if (request.getServings() != null)
//            recipe.setServings(request.getServings());
//
//        // Recalculate totals from ingredients
//        double totalCalories = 0;
//        double totalProtein = 0;
//        double totalCarbs = 0;
//        double totalFats = 0;
//
//        for (Ingredient ing : recipe.getIngredients()) {
//            totalCalories += ing.getCalories() != null ? ing.getCalories() : 0;
//            totalProtein += ing.getProtein() != null ? ing.getProtein() : 0;
//            totalCarbs += ing.getCarbs() != null ? ing.getCarbs() : 0;
//            totalFats += ing.getFats() != null ? ing.getFats() : 0;
//        }
//
//        recipe.setTotalCalories(totalCalories);
//        recipe.setTotalProtein(totalProtein);
//        recipe.setTotalCarbs(totalCarbs);
//        recipe.setTotalFats(totalFats);
//
//        return MapperUtil.toRecipeResponse(recipeRepository.save(recipe));
//    }
    @Override
    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeRequest request) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // 1️⃣ Update basic fields
        if (request.getTitle() != null) recipe.setTitle(request.getTitle());
        if (request.getDescription() != null) recipe.setDescription(request.getDescription());
        if (request.getCookingTime() != null) recipe.setCookingTime(request.getCookingTime());
        if (request.getServings() != null) recipe.setServings(request.getServings());

        // 2️⃣ Update ingredients
        if (request.getIngredients() != null) {

            // Decide: clear everything if user wants, or merge
            // If client sends all ingredients (edited list), just replace old ones
            recipe.getIngredients().clear();

            for (IngredientRequest ir : request.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(ir.getName());
                ingredient.setQuantity(ir.getQuantity());
                ingredient.setUnit(ir.getUnit());
                ingredient.setCalories(ir.getCalories());
                ingredient.setProtein(ir.getProtein());
                ingredient.setCarbs(ir.getCarbs());
                ingredient.setFats(ir.getFats());
                ingredient.setRecipe(recipe);
                recipe.getIngredients().add(ingredient);
            }
        }

        // 3️⃣ Recalculate totals from ingredients
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        for (Ingredient ing : recipe.getIngredients()) {
            totalCalories += ing.getCalories() != null ? ing.getCalories() : 0;
            totalProtein += ing.getProtein() != null ? ing.getProtein() : 0;
            totalCarbs += ing.getCarbs() != null ? ing.getCarbs() : 0;
            totalFats += ing.getFats() != null ? ing.getFats() : 0;
        }

        recipe.setTotalCalories(totalCalories);
        recipe.setTotalProtein(totalProtein);
        recipe.setTotalCarbs(totalCarbs);
        recipe.setTotalFats(totalFats);

        // 4️⃣ Save
        Recipe updated = recipeRepository.save(recipe);
        return MapperUtil.toRecipeResponse(updated);
    }


    // -------------------- Get Recipe By ID --------------------
    @Override
    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        return RecipeMapper.mapToRecipeResponse(recipe);

    }

    @Override
    public List<RecipeResponse> getUnapprovedRecipes() {
        return recipeRepository.findByApprovedFalseAndRejectedFalse()
                .stream()
                .map(recipeMapper::toResponse)
                .toList();
    }
    // AdminServiceImpl.java
    @Override

    public void rejectRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setRejected(true);
        recipe.setApproved(false); // optional: unapprove if rejected
        recipeRepository.save(recipe);
    }

    @Override
    public List<RecipeResponse> getRejectedRecipes() {
        return recipeRepository.findByRejectedTrue()
                .stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

}






