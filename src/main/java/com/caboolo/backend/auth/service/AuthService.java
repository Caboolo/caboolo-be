package com.caboolo.backend.auth.service;

import com.caboolo.backend.auth.domain.RefreshToken;
import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.auth.provider.OtpProvider;
import com.caboolo.backend.auth.repository.RefreshTokenRepository;
import com.caboolo.backend.userLogin.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final OtpProvider otpProvider;
    private final UserLoginService userLoginService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry:2592000000}")
    private long refreshTokenExpiry;

    public AuthService(OtpProvider otpProvider,
                       UserLoginService userLoginService,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.otpProvider = otpProvider;
        this.userLoginService = userLoginService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean sendOtp(String phoneNumber) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        log.info("Sending OTP via provider for masked phone: ******{}", maskPhoneNumber(normalizedPhone));
        otpProvider.sendOtp(normalizedPhone);
        return true;
    }

    public AuthResponse verifyOtp(String phoneNumber, String otp) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        log.info("Verifying OTP for masked phone: ******{}", maskPhoneNumber(normalizedPhone));

        boolean isValid = otpProvider.verifyOtp(normalizedPhone, otp);
        if (!isValid) {
            log.warn("Invalid OTP from provider for masked phone: ******{}", maskPhoneNumber(normalizedPhone));
            throw new RuntimeException("Invalid OTP");
        }

        log.info("OTP successfully verified by provider for masked phone: ******{}", maskPhoneNumber(normalizedPhone));

        // Handle user login (creates user if first login)
        AuthResponse authResponse = userLoginService.handleLogin(normalizedPhone);

        // Generate Tokens
        String accessToken = jwtService.generateAccessToken(authResponse.getUserId(), normalizedPhone, "USER");
        String refreshTokenString = jwtService.generateRefreshToken(authResponse.getUserId());

        // Save refresh token
        saveRefreshToken(authResponse.getUserId(), refreshTokenString);

        return AuthResponse.Builder.authResponse()
                .withUserId(authResponse.getUserId())
                .withMessage(authResponse.getMessage())
                .withPhoneNumber(authResponse.getPhoneNumber())
                .withIsProfileCreated(authResponse.isProfileCreated())
                .withAccessToken(accessToken)
                .withRefreshToken(refreshTokenString)
                .build();
    }

    public AuthResponse refreshToken(String refreshTokenString) {
        log.info("Refreshing token");
        String tokenHash = hashString(refreshTokenString);

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken token = tokenOpt.get();

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        // Revoke old token
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String userId = token.getUserId();
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(userId, "", "USER");
        String newRefreshTokenString = jwtService.generateRefreshToken(userId);

        saveRefreshToken(userId, newRefreshTokenString);

        return AuthResponse.Builder.authResponse()
                .withUserId(userId)
                .withMessage("Token refreshed")
                .withPhoneNumber("")
                .withIsProfileCreated(true)
                .withAccessToken(newAccessToken)
                .withRefreshToken(newRefreshTokenString)
                .build();
    }

    private void saveRefreshToken(String userId, String refreshTokenString) {
        String hash = hashString(refreshTokenString);
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plus(refreshTokenExpiry, ChronoUnit.MILLIS));
        token.setRevoked(false);
        refreshTokenRepository.save(token);
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash string", e);
        }
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return "+91" + digits;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
