//package com.recipe.backend.util;
//import com.recipe.backend.dto.*;
//import com.recipe.backend.model.*;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//import com.recipe.backend.util.TimeUtil;
//
//
//public class MapperUtil {
//
//    private MapperUtil() {}
//
//    public static UserResponse toUserResponse(User user) {
//        if (user == null) return null;
//
//        return UserResponse.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .role(user.getRole())
//                .enabled(user.isEnabled())
//                .recipeCount(0L)   // default, admin overrides this
//                .build();
//    }
//
//
//    public static List<UserResponse> toUserResponseList(List<User> users) {
//        return users.stream()
//                .map(MapperUtil::toUserResponse)
//                .collect(Collectors.toList());
//    }
//
//    // =========================
//    // USERS WITH RECIPES (ADMIN)
//    // =========================
//    public static UserWithRecipesResponse toUserWithRecipes(User user) {
//        if (user == null) return null;
//
//        List<RecipeResponse> recipes = user.getRecipes() == null
//                ? List.of()
//                : user.getRecipes().stream()
//                .map(MapperUtil::toRecipeResponse)
//                .collect(Collectors.toList());
//
//        return new UserWithRecipesResponse(
//                user.getId(),
//                user.getUsername(),
//                user.getEmail(),
//                recipes
//        );
//    }
//
//
//
//
//    // =========================
//    // RECIPES
//    // =========================
//    public static RecipeResponse toRecipeResponse(Recipe recipe) {
//        return RecipeResponse.builder()
//                .id(recipe.getId())
//                .title(recipe.getTitle())
//                .description(recipe.getDescription())
//                .servings(recipe.getServings())
//                .cookingTime(recipe.getCookingTime())
//                .approved(recipe.isApproved())
//                // 🔹 Calculate totals safely
//                .totalCalories(recipe.getIngredients() != null
//                        ? recipe.getIngredients().stream().mapToDouble(i -> i.getCalories() != null ? i.getCalories() : 0).sum() : 0)
//                .totalProtein(recipe.getIngredients() != null
//                        ? recipe.getIngredients().stream().mapToDouble(i -> i.getProtein() != null ? i.getProtein() : 0).sum() : 0)
//                .totalCarbs(recipe.getIngredients() != null
//                        ? recipe.getIngredients().stream().mapToDouble(i -> i.getCarbs() != null ? i.getCarbs() : 0).sum() : 0)
//                .totalFats(recipe.getIngredients() != null
//                        ? recipe.getIngredients().stream().mapToDouble(i -> i.getFats() != null ? i.getFats() : 0).sum() : 0)
//                // 🔹 Safe createdBy mapping
//                .createdById(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null)
//                .createdByUsername(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getUsername() : "Admin")
//                .createdByEmail(recipe.getCreatedBy() != null ? recipe.getCreatedBy().getEmail() : null)
//                .ingredients(recipe.getIngredients() != null
//                        ? recipe.getIngredients().stream()
//                        .map(ing -> IngredientResponse.builder()
//                                .name(ing.getName())
//                                .quantity(String.valueOf(ing.getQuantity()))
//                                .unit(ing.getUnit())
//                                .calories(ing.getCalories())
//                                .protein(ing.getProtein())
//                                .carbs(ing.getCarbs())
//                                .fats(ing.getFats())
//                                .build())
//                        .collect(Collectors.toSet())
//                        : new HashSet<>())
//                .createdAt(recipe.getCreatedAt())
//                .updatedAt(recipe.getUpdatedAt())
//                .build();
//
//    }
//
//    // ✅ Fixed: Accept Collection<Recipe> instead of Collection<Object>
//    public static List<RecipeResponse> toRecipeResponseList(Collection<Recipe> recipes) {
//        return recipes.stream()
//                .map(MapperUtil::toRecipeResponse)
//                .collect(Collectors.toList());
//    }
//
//    // =========================
//    // COMMENTS
//    // =========================
//
//    public static CommentResponse toCommentResponse(Comment comment, User currentUser) {
//
//        return CommentResponse.builder()
//                .id(comment.getId())
//                .content(comment.getContent())
//                .username(
//                        comment.getUser().getId().equals(currentUser.getId())
//                                ? "You"
//                                : comment.getUser().getUsername()
//                )
//                .userId(comment.getUser().getId())
//                .recipeId(comment.getRecipe().getId()) // ✅ NOW VALID
//                .createdAt(comment.getCreatedAt())
//                .timeAgo(TimeUtil.getTimeAgo(comment.getCreatedAt()))
//                .build();
//    }
//
//
//
//    public static List<CommentResponse> toCommentResponseList(
//            List<Comment> comments,
//            User currentUser
//    ) {
//        return comments.stream()
//                .map(comment -> toCommentResponse(comment, currentUser))
//                .collect(Collectors.toList());
//    }
//
//}








package com.recipe.backend.util;

import com.recipe.backend.dto.*;
import com.recipe.backend.model.*;
import java.util.Collection;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MapperUtil {

    private static final RecipeMapper recipeMapper = new RecipeMapper();

    private MapperUtil() {}

    /* =========================
       USERS
       ========================= */

    public static UserResponse toUserResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .recipeCount(0L)
                .build();
    }

    public static List<UserResponse> toUserResponseList(List<User> users) {
        return users.stream()
                .map(MapperUtil::toUserResponse)
                .collect(Collectors.toList());
    }

    /* =========================
       USERS WITH RECIPES
       (signature unchanged)
       ========================= */

    public static UserWithRecipesResponse toUserWithRecipes(User user) {
        if (user == null) return null;

        List<RecipeResponse> recipes = user.getRecipes() == null
                ? List.of()
                : user.getRecipes().stream()
                .map(recipeMapper::toResponse)   // ✅ delegated
                .collect(Collectors.toList());

        return new UserWithRecipesResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                recipes
        );
    }

    /* =========================
       COMMENTS
       ========================= */

    public static CommentResponse toCommentResponse(Comment comment, User currentUser) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(
                        comment.getUser().getId().equals(currentUser.getId())
                                ? "You"
                                : comment.getUser().getUsername()
                )
                .userId(comment.getUser().getId())
                .recipeId(comment.getRecipe().getId())
                .createdAt(comment.getCreatedAt())
                .timeAgo(TimeUtil.getTimeAgo(comment.getCreatedAt()))
                .build();
    }

    public static List<CommentResponse> toCommentResponseList(
            List<Comment> comments,
            User currentUser
    ) {
        return comments.stream()
                .map(comment -> toCommentResponse(comment, currentUser))
                .collect(Collectors.toList());
    }
    /* =========================
   RECIPES (DELEGATION ONLY)
   ========================= */

    public static RecipeResponse toRecipeResponse(Recipe recipe) {
        if (recipe == null) return null;
        return recipeMapper.toResponse(recipe); // ✅ delegate
    }

    public static List<RecipeResponse> toRecipeResponseList(
            Collection<Recipe> recipes
    ) {
        if (recipes == null) return List.of();

        return recipes.stream()
                .map(recipeMapper::toResponse) // ✅ delegate
                .collect(Collectors.toList());
    }

}
