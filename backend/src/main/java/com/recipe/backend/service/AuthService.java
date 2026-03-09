    package com.recipe.backend.service;

    import com.recipe.backend.Security.JwtUtil;
    import com.recipe.backend.dto.CommentResponse;
    import com.recipe.backend.model.LoginRequest;
    import com.recipe.backend.model.User;
    import com.recipe.backend.repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;

    import java.util.List;
    import java.util.Random;

    @Service
    public class AuthService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private EmailService emailService;

        private final Random random = new Random();

        /* ================= REGISTER ================= */
        public void register(String name, String email, String password) {
            validateGmail(email);
            String normalizedEmail = email.toLowerCase().trim();

            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setUsername(name);
            user.setEmail(normalizedEmail);
            user.setPassword(passwordEncoder.encode(password.trim()));
            user.setOtp(generateOTP());

            userRepository.save(user);

            emailService.sendOTPEmail(normalizedEmail, user.getOtp());
        }

        /* ================= LOGIN ================= */



        /* ================= FORGOT PASSWORD ================= */
        public void sendForgotPasswordOTP(String email) {

            String normalizedEmail = email.toLowerCase().trim();

            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setOtp(generateOTP());
            userRepository.save(user);

            emailService.sendOTPEmail(normalizedEmail, user.getOtp());
        }

        /* ================= RESET PASSWORD ================= */
        public void resetPassword(String email, String otp, String newPassword) {

            String normalizedEmail = email.toLowerCase().trim();

            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getOtp() == null || !user.getOtp().equals(otp)) {
                throw new RuntimeException("Invalid OTP");
            }

            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            user.setOtp(null);
            userRepository.save(user);
        }

        /* ================= OTP ================= */
        private String generateOTP() {
            return String.valueOf(100000 + random.nextInt(900000));
        }

        private void validateGmail(String email) {
            if (email == null || !email.toLowerCase().endsWith("@gmail.com")) {
                throw new IllegalArgumentException("Only Gmail addresses are allowed");
            }
        }
        public User getCurrentUserOrNull() {
            try {
                return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } catch (Exception e) {
                return null;
            }
        }
        public User authenticate(LoginRequest request) {
            // 1️⃣ Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2️⃣ Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            // 3️⃣ Return authenticated user
            return user;
        }
    }


