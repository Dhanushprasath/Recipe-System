package com.recipe.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class UserWithRecipesResponse {
    private Long userId;
    private String username;
    private String email;
    private List<RecipeResponse> recipes;
}


