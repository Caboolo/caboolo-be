package com.caboolo.backend.auth.controller;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.dto.LoginRequestDto;
import com.caboolo.backend.auth.service.AuthService;
import com.caboolo.backend.userLogin.service.UserLoginService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final AuthService authService;
    private final UserLoginService userLoginService;

    public AuthController(AuthService authService, UserLoginService userLoginService) {
        this.authService = authService;
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
    public RestEntity<AuthResponse> login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("Received login request for phone: {}", loginRequestDto.getPhoneNumber());
        try {
            FirebaseToken decodedToken = authService.verifyToken(loginRequestDto.getIdToken());
            String uid = decodedToken.getUid();
            log.debug("Token verified for uid: {}", uid);
            AuthResponse authResponse = userLoginService.handleLogin(uid, loginRequestDto.getPhoneNumber());
            log.info("Login successful for user: {}", uid);
            return successResponse(authResponse, "Login successful");
        } catch (FirebaseAuthException e) {
            log.warn("Login failed: Invalid or expired token. Error: {}", e.getMessage());
            return errorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }
}
