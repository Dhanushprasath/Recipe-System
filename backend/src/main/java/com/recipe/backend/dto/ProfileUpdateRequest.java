package com.recipe.backend.dto;

import com.recipe.backend.model.Gender;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String username;
    private Integer age;
    private Gender gender;
    private Double height;
    private Double weight;
    private String activityLevel; // or ActivityLevel type
    private String goal;          // or Goal type
    private String goalIntensity; // or GoalIntensity typ


}
