//package com.recipe.backend.controller;
//
//    import com.recipe.backend.dto.CommentRequest;
//    import com.recipe.backend.dto.CommentResponse;
//    import com.recipe.backend.model.User;
//    import com.recipe.backend.repository.UserRepository;
//    import com.recipe.backend.service.CommentService;
//    import lombok.RequiredArgsConstructor;
//    import org.springframework.security.core.Authentication;
//    import org.springframework.security.core.annotation.AuthenticationPrincipal;
//    import org.springframework.web.bind.annotation.*;
//
//    import java.util.List;
//
//    @RestController
//    @RequestMapping("/api/comments")
//    @RequiredArgsConstructor
//    public class CommentController {
//
//        private final CommentService commentService;
//        private final UserRepository userRepository;
//
//        @PostMapping("/{recipeId}")
//        public CommentResponse addComment(
//                @PathVariable Long recipeId,
//                @RequestBody CommentRequest request,
//                Authentication authentication
//        ) {
//            User user = userRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            return commentService.addComment(recipeId, request.getContent(), request.getParentId(), user);
//        }
//
//
//
//        @DeleteMapping("/{commentId}")
//        public void deleteComment(
//                @PathVariable Long commentId,
//                Authentication authentication
//        ) {
//            User user = userRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            commentService.deleteComment(commentId, user);
//        }
//
//
//        @GetMapping("/recipe/{recipeId}")
//        public List<CommentResponse> getComments(
//                @PathVariable Long recipeId,
//                Authentication authentication
//        ) {
//            User viewer = userRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            return commentService.getCommentsByRecipe(recipeId, viewer);
//        }
//    }


package com.recipe.backend.controller;

import com.recipe.backend.dto.CommentRequest;
import com.recipe.backend.dto.CommentResponse;
import com.recipe.backend.model.Comment;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    // Add comment / reply
    @PostMapping("/{recipeId}")
    public CommentResponse addComment(
            @PathVariable Long recipeId,
            @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return commentService.addComment(recipeId, request, userDetails);
    }

    // Delete comment
    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        commentService.deleteComment(commentId, user);
    }

    // Get all comments for a recipe
    @GetMapping("/recipe/{recipeId}")
    public List<CommentResponse> getComments(
            @PathVariable Long recipeId,
            Authentication authentication
    ) {
        User viewer = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return commentService.getCommentsByRecipe(recipeId, viewer);
    }
}
