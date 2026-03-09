package com.recipe.backend.dto;


import com.recipe.backend.model.ActivityLevel;
import com.recipe.backend.model.Gender;
import com.recipe.backend.model.Goal;
import com.recipe.backend.model.GoalIntensity;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
    public class ProfileRequest {
        private Integer age;
    private String username;
        private Gender gender;
        private Double height;
        private Double weight;
    private ActivityLevel activityLevel;
    private Goal goal;
    private GoalIntensity goalIntensity;
    }

