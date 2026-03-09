package com.recipe.backend.dto;


import com.recipe.backend.model.Profile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.recipe.backend.model.Gender;

@Getter
@Setter
@Builder
public class ProfileResponse {

    private String username;
    private String email;
    private String role;
    private Integer age;
    private Gender gender;
    private Double height;
    private Double weight;

    private Double bmi;
    private String bmiCategory;
    private Double dailyCalories;


    private Double maintenanceCalories;
    private Double weightLossCalories;
    private Double weightGainCalories;
    private Double bmr;
    private String goal;
    private String activityLevel;
    private String goalIntensity;

}

