package com.recipe.backend.controller;

import com.recipe.backend.Security.JwtService;
import com.recipe.backend.dto.ProfileRequest;
import com.recipe.backend.dto.ProfileResponse;
import com.recipe.backend.dto.ProfileUpdateRequest;
import com.recipe.backend.model.*;
import com.recipe.backend.repository.ProfileRepository;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.AuthService;
import com.recipe.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class profileController {

    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;



    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String email = auth.getName();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Try to fetch profile
            ProfileResponse profile = profileService.getProfile(email);

            if (profile == null) {
                // profile not completed yet
                return ResponseEntity.ok(Map.of(
                        "profileCompleted", false,
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                ));
            }

            // profile exists → send full data + profileCompleted true
            Map<String, Object> response = new HashMap<>();
            response.put("profileCompleted", true);
            response.put("username", profile.getUsername());
            response.put("email", profile.getEmail());
            response.put("role", profile.getRole());
            response.put("age", profile.getAge());
            response.put("gender", profile.getGender());
            response.put("height", profile.getHeight());
            response.put("weight", profile.getWeight());
            response.put("bmi", profile.getBmi());
            response.put("bmiCategory", profile.getBmiCategory());
            response.put("bmr", profile.getBmr()); // ✅ ADD

            response.put("dailyCalories", profile.getDailyCalories());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // fallback
            return ResponseEntity.ok(Map.of(
                    "profileCompleted", false,
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().name()
            ));
        }
    }


    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = auth.getName();

        // ✅ Call service that handles enums and saving correctly
        ProfileResponse updatedProfile = profileService.updateProfile(email, request);

        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/profile/preview-calories")
    public ResponseEntity<?> previewCalories(
            @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.previewCalories(request);
        return ResponseEntity.ok(response);
    }


}



