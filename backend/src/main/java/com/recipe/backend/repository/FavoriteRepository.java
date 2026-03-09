package com.recipe.backend.repository;

import com.recipe.backend.model.Favorite;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndRecipe(User user, Recipe recipe);
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndRecipe(User user, Recipe recipe);
}




