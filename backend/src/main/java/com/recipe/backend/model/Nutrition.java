//    package com.recipe.backend.model;
//
//    import com.fasterxml.jackson.annotation.JsonBackReference;
//    import com.fasterxml.jackson.annotation.JsonIgnore;
//    import jakarta.persistence.*;
//    import lombok.*;
//
//    @Entity
//    @Table(name = "nutrition")
//    @Getter
//    @Setter
//    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public class Nutrition {
//
//        @Id
//        @GeneratedValue(strategy = GenerationType.IDENTITY)
//        @EqualsAndHashCode.Include
//        private Long id;
//        private String name;
//
//        private String ingredientName;
//
//        @Builder.Default
//        private Double calories = 0.0;
//
//        @Builder.Default
//        private Double protein = 0.0;
//
//        @Builder.Default
//        private Double carbs = 0.0;
//
//        @Builder.Default
//        private Double fats = 0.0;
//
//        private String source;
//
//        @Builder.Default
//        private Boolean fromCache = false;
//
//        @ManyToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "recipe_id")
//        @JsonBackReference
//        private Recipe recipe;
//        private String unit; // "g", "ml", "tsp"
//
//
//        public Nutrition(int i, int i1, int i2, int i3, int i4, int i5) {
//        }
//    }


package com.recipe.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.recipe.backend.dto.FallbackNutritionDTO;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "nutrition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Nutrition  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;
    private String ingredientName;

    @Builder.Default
    private Double calories = 0.0;

    @Builder.Default
    private Double protein = 0.0;

    @Builder.Default
    private Double carbs = 0.0;

    @Builder.Default
    private Double fats = 0.0;

    private String source;

    @Builder.Default
    private Boolean fromCache = false;

    private String unit; // "g", "ml", "tsp"


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @JsonBackReference
    private Recipe recipe;
    public Nutrition(Double calories, Double protein, Double carbs, Double fats, String ingredientName) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredientName = ingredientName;
        this.source = "fallback";
        this.fromCache = true;
        this.unit = "g";
    }

}
