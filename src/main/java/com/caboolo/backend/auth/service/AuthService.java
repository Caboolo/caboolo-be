package com.caboolo.backend.auth.service;

import com.caboolo.backend.auth.domain.RefreshToken;
import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.auth.repository.RefreshTokenRepository;
import com.caboolo.backend.userLogin.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final Msg91Service msg91Service;
    private final UserLoginService userLoginService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${redis.otp-ttl:300}")
    private long otpTtl;

    @Value("${jwt.refresh-token-expiry:2592000000}")
    private long refreshTokenExpiry;

    public AuthService(StringRedisTemplate redisTemplate,
                       Msg91Service msg91Service,
                       UserLoginService userLoginService,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.redisTemplate = redisTemplate;
        this.msg91Service = msg91Service;
        this.userLoginService = userLoginService;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean sendOtp(String phoneNumber) {
        // Normalize phone number
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        log.info("Sending OTP for phone: {}", normalizedPhone);

        // Generate 6 digit secure OTP
        String otp = generateOtp();
        String otpHash = hashString(otp);

        String redisKey = "otp:" + normalizedPhone;
        String attemptsKey = "otp_attempts:" + normalizedPhone;

        // Store hash in redis
        redisTemplate.opsForValue().set(redisKey, otpHash, otpTtl, TimeUnit.SECONDS);
        redisTemplate.delete(attemptsKey);

        // Send SMS
        return msg91Service.sendOtp(normalizedPhone, otp);
    }

    public AuthResponse verifyOtp(String phoneNumber, String otp) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        log.info("Verifying OTP for phone: {}", normalizedPhone);

        String redisKey = "otp:" + normalizedPhone;
        String attemptsKey = "otp_attempts:" + normalizedPhone;

        String storedHash = redisTemplate.opsForValue().get(redisKey);

        if (storedHash == null) {
            log.warn("OTP not found or expired for phone: {}", normalizedPhone);
            throw new RuntimeException("OTP expired or not found");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts > 5) {
            redisTemplate.delete(redisKey);
            redisTemplate.delete(attemptsKey);
            log.warn("Max OTP attempts exceeded for phone: {}", normalizedPhone);
            throw new RuntimeException("Max OTP attempts exceeded");
        }

        String inputHash = hashString(otp);
        if (!storedHash.equals(inputHash)) {
            log.warn("Invalid OTP for phone: {}", normalizedPhone);
            throw new RuntimeException("Invalid OTP");
        }

        // Verification successful, delete OTP
        redisTemplate.delete(redisKey);
        redisTemplate.delete(attemptsKey);

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
        String newAccessToken = jwtService.generateAccessToken(userId, "", "USER"); // We don't have phone here unless fetched from DB, but usually JWT refresh doesn't strictly need phone number if not used, or we fetch it. Let's fetch it via UserLoginService if needed. For now, empty or from existing logic.
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

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
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
        // Remove non-numeric characters
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return "+91" + digits; // Assuming India E.164 based on request
    }
}
