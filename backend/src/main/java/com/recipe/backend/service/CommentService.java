//package com.recipe.backend.service;
//
//import com.recipe.backend.dto.CommentRequest;
//import com.recipe.backend.dto.CommentResponse;
//import com.recipe.backend.model.Comment;
//import com.recipe.backend.model.Recipe;
//import com.recipe.backend.model.Role;
//import com.recipe.backend.model.User;
//import com.recipe.backend.repository.CommentRepository;
//import com.recipe.backend.repository.RecipeRepository;
//import com.recipe.backend.repository.UserRepository;
//import com.recipe.backend.util.TimeUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class CommentService {
//    private final UserRepository userRepository;
//    private final CommentRepository commentRepository;
//    private final RecipeRepository recipeRepository;
//
//    /* ================= ADD COMMENT / REPLY ================= */
//    public Comment addComment(Long recipeId, CommentRequest request, UserDetails userDetails) {
//
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Recipe recipe = recipeRepository.findById(recipeId)
//                .orElseThrow(() -> new RuntimeException("Recipe not found"));
//
//        Comment comment = new Comment();
//        comment.setContent(request.getContent());
//        comment.setCreatedAt(LocalDateTime.now());
//        comment.setUser(user);
//        comment.setRecipe(recipe);
//
//        if (request.getParentId() != null) {
//            Comment parent = commentRepository.findById(request.getParentId())
//                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
//            comment.setParentComment(parent); // ← important
//        }
//
//        return commentRepository.save(comment);
//    }
//
//    /* ================= DELETE COMMENT ================= */
//    public void deleteComment(Long commentId, User currentUser) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment not found"));
//
//        boolean isOwner = comment.getUser().getId().equals(currentUser.getId());
//        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
//
//        if (!isOwner && !isAdmin) throw new RuntimeException("Unauthorized");
//
//        commentRepository.delete(comment);
//    }
//
//    /* ================= GET COMMENTS ================= */
//    public List<CommentResponse> getCommentsByRecipe(Long recipeId, User currentUser) {
//
//        List<Comment> comments = commentRepository
//                .findByRecipeIdAndParentCommentIsNullOrderByCreatedAtDesc(recipeId);
//
//        return comments.stream()
//                .map(c -> mapToResponseWithReplies(c, currentUser))
//                .collect(Collectors.toList());
//    }
//
//    /* ================= MAPPING ================= */
//    private CommentResponse mapToResponse(Comment comment, User currentUser) {
//
//        boolean isOwner = currentUser != null &&
//                comment.getUser() != null &&
//                comment.getUser().getId().equals(currentUser.getId());
//
//        String displayName = comment.getUser() != null ? comment.getUser().getUsername() : "Unknown";
//
//
//        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;
//
//        return CommentResponse.builder()
//                .id(comment.getId())
//                .content(comment.getContent())
//                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
//                .username(displayName)
//                .isOwner(isOwner)
//                .isAdmin(isAdmin)
//                .recipeId(comment.getRecipe() != null ? comment.getRecipe().getId() : null)
//                .createdAt(comment.getCreatedAt())
//                .timeAgo(TimeUtil.getTimeAgo(comment.getCreatedAt()))
//                .build();
//    }
//
//    private CommentResponse mapToResponseWithReplies(Comment comment, User currentUser) {
//        CommentResponse response = mapToResponse(comment, currentUser);
//        List<CommentResponse> replies = comment.getReplies().stream()
//                .map(r -> mapToResponseWithReplies(r, currentUser))
//                .collect(Collectors.toList());
//        response.setReplies(replies);
//        return response;
//    }
//}








package com.recipe.backend.service;

import com.recipe.backend.dto.CommentRequest;
import com.recipe.backend.dto.CommentResponse;
import com.recipe.backend.model.Comment;
import com.recipe.backend.model.Recipe;
import com.recipe.backend.model.Role;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.CommentRepository;
import com.recipe.backend.repository.RecipeRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;

    /* ================= ADD COMMENT / REPLY ================= */

    @Transactional
    public CommentResponse addComment(Long recipeId, CommentRequest request, UserDetails userDetails) {

        // 1️⃣ Fetch the managed User entity from DB
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Fetch the managed Recipe entity from DB
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // 3️⃣ Create comment and set relationships
        Comment comment = Comment.builder()
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .user(user)      // Must be managed
                .recipe(recipe)  // Must be managed
                .build();

        // 4️⃣ Handle reply (optional parent comment)
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        // 5️⃣ Save comment
        Comment saved = commentRepository.save(comment);

        // 6️⃣ Return DTO
        return mapToResponse(saved, user);
    }

    /* ================= DELETE COMMENT ================= */
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isOwner = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) throw new RuntimeException("Unauthorized");

        commentRepository.delete(comment);
    }

    /* ================= GET COMMENTS ================= */
    public List<CommentResponse> getCommentsByRecipe(Long recipeId, User currentUser) {

        List<Comment> comments = commentRepository
                .findByRecipeIdAndParentCommentIsNullOrderByCreatedAtDesc(recipeId);

        return comments.stream()
                .map(c -> mapToResponseWithReplies(c, currentUser))
                .collect(Collectors.toList());
    }

    /* ================= MAPPING ================= */
    private CommentResponse mapToResponse(Comment comment, User currentUser) {

        boolean isOwner = currentUser != null &&
                comment.getUser() != null &&
                comment.getUser().getId().equals(currentUser.getId());

        String displayName = comment.getUser() != null ? comment.getUser().getUsername() : "Unknown";

        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .username(displayName)
                .isOwner(isOwner)
                .isAdmin(isAdmin)
                .recipeId(comment.getRecipe() != null ? comment.getRecipe().getId() : null)
                .createdAt(comment.getCreatedAt())
                .timeAgo(TimeUtil.getTimeAgo(comment.getCreatedAt()))
                .build();
    }

    private CommentResponse mapToResponseWithReplies(Comment comment, User currentUser) {
        CommentResponse response = mapToResponse(comment, currentUser);
        List<CommentResponse> replies = comment.getReplies().stream()
                .map(r -> mapToResponseWithReplies(r, currentUser))
                .collect(Collectors.toList());
        response.setReplies(replies);
        return response;
    }
}
