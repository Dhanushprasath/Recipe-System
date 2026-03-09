package com.recipe.backend.service;
import com.recipe.backend.model.Comment;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.CommentRepository;
import com.recipe.backend.repository.RecipeRepository;
import com.recipe.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;



    // -------------------- User Management --------------------
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) throws Exception {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found"));

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword());
        existing.setRole(user.getRole());

        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found"));
        userRepository.delete(user);
    }

    @Override
    public User getUserById(Long id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // -------------------- Admin Operations --------------------
    @Override
    public void deleteCommentByAdmin(Long commentId) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new Exception("Comment not found"));
        commentRepository.delete(comment);
    }

    @Override
    public Object getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalRecipes = recipeRepository.count();
        long totalComments = commentRepository.count();


        return Map.of(
                "totalUsers", totalUsers,
                "totalRecipes", totalRecipes,
                "totalComments", totalComments
        );
    }

    @Override
    public User getCurrentUser(Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public void updateUsername(String email, String username) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(username);
        userRepository.save(user);
    }
}







