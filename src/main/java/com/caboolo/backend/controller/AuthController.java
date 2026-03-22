package com.caboolo.backend.controller;

import com.caboolo.backend.dto.AuthResponse;
import com.caboolo.backend.dto.LoginRequest;
import com.caboolo.backend.model.User;
import com.caboolo.backend.repository.UserRepository;
import com.caboolo.backend.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
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

            Optional<User> existingUserOpt = userRepository.findByFirebaseUid(uid);
            User user;

            if (existingUserOpt.isEmpty()) {
                user = new User(uid, phoneNumber);
                user = userRepository.save(user);
            } else {
                user = existingUserOpt.get();
                // Update phone number if missing or changed
                if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
                    user.setPhoneNumber(phoneNumber);
                    user = userRepository.save(user);
                }
            }

            return ResponseEntity.ok(new AuthResponse("Login successful", user.getPhoneNumber() != null ? user.getPhoneNumber() : "UID: " + uid));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid or expired token", null));
        }
    }
}
