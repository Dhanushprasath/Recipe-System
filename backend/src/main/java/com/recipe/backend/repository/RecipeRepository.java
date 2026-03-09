package com.recipe.backend.repository;

import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.Role;
import com.recipe.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @Query("""
            SELECT r FROM Recipe r
            WHERE r.approved = true AND r.createdBy.id <> :currentUserId
            """)
    List<Recipe> findApprovedRecipesForOtherUsers(@Param("currentUserId") Long currentUserId);

    @Query("""
                   SELECT r FROM Recipe r
                   WHERE (r.createdBy.role = 'ADMIN' OR (r.createdBy.role = 'USER' AND r.approved = true))
                     AND r.createdBy.id <> :currentAdminId
            """)
    List<Recipe> findAllOtherRecipesForAdmin(@Param("currentAdminId") Long currentAdminId);


    List<Recipe> findByCreatedBy(User user);

    List<Recipe> findByApprovedFalseAndCreatedBy_Role(Role role);

    List<Recipe> findByCreatedBy_Email(String email);

    Page<Recipe> findByCreatedByNot(User user, PageRequest pageRequest);

    List<Recipe> findByApprovedTrueAndCreatedBy_IdNot(Long userId);

    List<Recipe> findByApprovedFalse();

    List<Recipe> findByApprovedFalseAndRejectedFalse();

    List<Recipe> findByApprovedTrueAndCreatedByNotOrCreatedBy(User excludeUser, User adminUser);

    List<Recipe> findByApprovedTrueAndCreatedByNot(User user);

    long countByCreatedBy_IdAndApprovedTrue(Long userId);

    List<Recipe> findByCreatedBy_IdAndApprovedTrue(Long userId);
    List<Recipe> findByCreatedBy_IdNot(Long userId);


        long countByApprovedFalseAndRejectedFalse();
    long countByApproved(Boolean approved);

    List<Recipe> findByRejectedTrue();





    @Query("""
SELECT DISTINCT r
FROM Recipe r
LEFT JOIN FETCH r.ingredients
WHERE r.createdBy.email = :email
""")
    List<Recipe> findAdminRecipesWithNutrition(@Param("email") String email);


    @Query("""
SELECT r FROM Recipe r
LEFT JOIN FETCH r.ingredients
WHERE r.id = :id
""")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);


}

