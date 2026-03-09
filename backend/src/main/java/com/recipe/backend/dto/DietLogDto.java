package com.recipe.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietLogDto {
    private Long userId;       // optional, ID of the user
    private int calories;      // total calories consumed today
    private int protein;       // optional: total protein
    private int carbs;         // optional: total carbs
    private int fat;           // optional: total fat
}

