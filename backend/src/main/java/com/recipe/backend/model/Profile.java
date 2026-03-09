package com.recipe.backend.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    private int age;
    private Double height; // cm
    private Double weight; // kg
    private Double bmi;
    private String bmiCategory;
    private Integer dailyCalories;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    private GoalIntensity goalIntensity;

    private boolean profileCompleted;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



}
