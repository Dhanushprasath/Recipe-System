//package com.recipe.backend.repository;
//
//import com.recipe.backend.model.Role;
//import com.recipe.backend.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByEmail(String email);
//    boolean existsByEmail(String email);
//    boolean existsByUsername(String username);
//
//    // Corrected method
//    List<User> findByRole(Role role);
//
//    Optional<User> findByUsername(String username);
//
//}



package com.recipe.backend.repository;

import com.recipe.backend.model.Role;
import com.recipe.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

//    // Works now because User entity has the 'role' field
    List<User> findByRole(Role role);


    Optional<User> findByUsername(String username);

}
