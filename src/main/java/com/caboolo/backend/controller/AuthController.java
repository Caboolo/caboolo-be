package com.caboolo.backend.controller;

import com.caboolo.backend.dto.AuthResponse;
import com.caboolo.backend.dto.LoginRequest;
import com.caboolo.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(loginRequest.getIdToken());
            String uid = decodedToken.getUid();
            
            // Extract phone number from claims
            String phoneNumber = null;
            if (decodedToken.getClaims().containsKey("phone_number")) {
                phoneNumber = (String) decodedToken.getClaims().get("phone_number");
            }
            
            // Here you would typically check if the user exists in your database using uid or phoneNumber,
            // and if not, create a new user record.
            // Then, you could issue your own application-specific JWT or just rely on Firebase tokens.

            return ResponseEntity.ok(new AuthResponse("Login successful", phoneNumber != null ? phoneNumber : "UID: " + uid));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid or expired token", null));
        }
    }
}
