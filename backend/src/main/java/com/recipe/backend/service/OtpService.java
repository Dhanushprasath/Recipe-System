package com.recipe.backend.service;

import com.recipe.backend.model.Otp;
import com.recipe.backend.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    // Generate OTP (for forgot password)
    public String generateOtp(String email) {

        otpRepository.deleteByEmail(email);

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);

        return otp;
    }

    // Verify OTP
    public boolean verifyOtp(String email, String otp) {

        Otp otpEntity = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            throw new RuntimeException("OTP expired");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepository.deleteByEmail(email);
        return true;
    }
}
