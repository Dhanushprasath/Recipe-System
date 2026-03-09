//package com.recipe.backend.repository;
//
//import com.recipe.backend.model.Otp;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface OtpRepository extends JpaRepository<Otp, Long> {
//    Optional<Otp> findTopByEmailOrderByExpiryTimeDesc(String email);
//}


package com.recipe.backend.repository;

import com.recipe.backend.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmail(String email);

    void deleteByEmail(String email);
}


