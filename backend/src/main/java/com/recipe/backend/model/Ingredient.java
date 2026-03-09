package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.recipe.backend.dto.IngredientRequest;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double quantity;
    private String unit;

    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double carbs;

    @Column(nullable = false)
    private Double fats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id",nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Recipe recipe;

    public IngredientRequest getNutrition() {
        return IngredientRequest.builder()
                .name(this.name)
                .quantity(this.quantity)
                .unit(this.unit)
                .calories(this.calories)
                .protein(this.protein)
                .carbs(this.carbs)
                .fats(this.fats)

                .build();
    }


}
