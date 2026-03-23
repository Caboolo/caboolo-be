package com.caboolo.backend.auth.controller;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.dto.LoginRequest;
import com.caboolo.backend.user.domain.User;
import com.caboolo.backend.user.service.UserService;
import com.caboolo.backend.auth.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public RestEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(loginRequest.getIdToken());
            String uid = decodedToken.getUid();
            
            // Extract phone number from claims
            String phoneNumber = null;
            if (decodedToken.getClaims().containsKey("phone_number")) {
                phoneNumber = (String) decodedToken.getClaims().get("phone_number");
            }

            User user = userService.handleLogin(uid, phoneNumber);

            AuthResponse responseBody = new AuthResponse("Login successful", user.getPhoneNumber() != null ? user.getPhoneNumber() : "UID: " + uid);
            return successResponse(responseBody, "Login successful");
        } catch (FirebaseAuthException e) {
            return errorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }
}
