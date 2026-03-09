//package com.recipe.backend.repository;
//
//
//
//import com.recipe.backend.model.Nutrition;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.Optional;
//
//public interface NutritionRepository extends JpaRepository<Nutrition, Long> {
//    Optional<Nutrition> findByIngredientName(String ingredientName);
//
//    Optional<Nutrition> findByIngredientNameIgnoreCase(String canonical);
//}


package com.recipe.backend.repository;

import com.recipe.backend.model.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {
    Optional<Nutrition> findByIngredientNameIgnoreCase(String ingredientName);
    Optional<Nutrition> findByIngredientName(String ingredientName);

        Optional<Nutrition> findByRecipeId(Long recipeId);

}
