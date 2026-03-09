    //package com.recipe.backend.repository;
    //
    //import com.recipe.backend.model.Comment;
    //import org.springframework.data.jpa.repository.JpaRepository;
    //
    //import java.util.List;
    //
    //public interface CommentRepository extends JpaRepository<Comment, Long> {
    //
    //    List<Comment> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
    //}

    package com.recipe.backend.repository;
    import com.recipe.backend.model.Comment;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Modifying;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

    import java.util.List;

    public interface CommentRepository extends JpaRepository<Comment, Long> {

        // Parent comments only
        List<Comment> findByRecipeIdAndParentCommentIsNullOrderByCreatedAtDesc(Long recipeId);

        // Replies
        @Modifying
        @Query("DELETE FROM Comment c WHERE c.recipe.id = :recipeId")
        void deleteByRecipeId(@Param("recipeId") Long recipeId);

//        List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentId);
//        void deleteByRecipeId(Long recipeId);
    }
