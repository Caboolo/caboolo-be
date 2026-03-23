package com.caboolo.backend.auth.controller;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.dto.LoginRequest;
import com.caboolo.backend.auth.service.AuthService;
import com.caboolo.backend.userLogin.service.UserLoginService;
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
    private final UserLoginService userLoginService;

    public AuthController(AuthService authService, UserLoginService userLoginService) {
        this.authService = authService;
        this.userLoginService = userLoginService;
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

            AuthResponse authResponse = userLoginService.handleLogin(uid, phoneNumber);

            return successResponse(authResponse, "Login successful");
        } catch (FirebaseAuthException e) {
            return errorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }
}
