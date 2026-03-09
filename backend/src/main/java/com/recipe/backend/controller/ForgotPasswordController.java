
package com.recipe.backend.controller;

import com.recipe.backend.dto.OtpRequest;
import com.recipe.backend.model.User;
import com.recipe.backend.repository.UserRepository;
import com.recipe.backend.service.EmailService;
import com.recipe.backend.service.OtpService;
import com.recipe.backend.util.ResetTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.Map;

@RestController
@RequestMapping("/auth")
public class
ForgotPasswordController {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public ForgotPasswordController(UserRepository userRepository,
                                    OtpService otpService,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // STEP 1: Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // STEP 2: Verify OTP
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest request) {

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        String resetToken = ResetTokenUtil.generateToken();

        return ResponseEntity.ok(
                Map.of(
                        "message", "OTP verified",
                        "resetToken", resetToken
                )
        );
    }

    // STEP 3: Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String newPassword = request.get("newPassword");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(newPassword); // encrypt in real app
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}

