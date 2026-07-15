package com.caboolo.backend.auth.controller;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.auth.dto.RefreshTokenRequest;
import com.caboolo.backend.auth.dto.SendOtpRequest;
import com.caboolo.backend.auth.dto.VerifyOtpRequest;
import com.caboolo.backend.auth.service.AuthService;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Send OTP", description = "Sends a 6-digit OTP to the user's phone number via 2Factor.")
    @PostMapping("/send-otp")
    public RestEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("Received send-otp request for phone: {}", request.getPhoneNumber());
        boolean success = authService.sendOtp(request.getPhoneNumber());
        if (success) {
            return successResponse("OTP sent successfully", "OTP Sent");
        } else {
            return errorResponse("Failed to send OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Verify OTP", description = "Verifies the 6-digit OTP using 2Factor and returns JWT tokens.")
    @PostMapping("/verify-otp")
    public RestEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Received verify-otp request for phone: {}", request.getPhoneNumber());
        try {
            AuthResponse authResponse = authService.verifyOtp(request.getPhoneNumber(), request.getOtp());
            log.info("Login successful for user: {}", authResponse.getUserId());
            return successResponse(authResponse, "Login successful");
        } catch (Exception e) {
            log.warn("Login failed: {}", e.getMessage());
            return errorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh")
    public RestEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        try {
            AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
            return successResponse(authResponse, "Token refreshed");
        } catch (Exception e) {
            log.warn("Refresh failed: {}", e.getMessage());
            return errorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
