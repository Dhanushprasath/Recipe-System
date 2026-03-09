package com.recipe.backend.service;


import com.recipe.backend.model.User;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    // ---------------- USER MANAGEMENT ----------------

    User createUser(User user);

    User updateUser(Long id, User user) throws Exception;

    void deleteUser(Long id) throws Exception;

    User getUserById(Long id) throws Exception;

    List<User> getAllUsers();
    User getCurrentUser(Authentication authentication);

    void updateUsername(String email, String username);

    // ---------------- ADMIN OPERATIONS ----------------

    void deleteCommentByAdmin(Long commentId) throws Exception;

    Object getDashboardStats();
}


