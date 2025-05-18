package com.secure.registration.token;

import com.secure.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class OtpTokenService {

    private final OtpTokenRepository otpTokenRepository;

    public String generateOtp(AppUser appUser) {
        // Generate 4 digit OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        
        OtpToken otpToken = new OtpToken(
                otp,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5), // OTP expires in 5 minutes
                appUser
        );

        otpTokenRepository.save(otpToken);

        return otp;
    }

    public OtpToken getOtp(String token) {
        return otpTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));
    }

    public int setConfirmedAt(String token) {
        return otpTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
} 