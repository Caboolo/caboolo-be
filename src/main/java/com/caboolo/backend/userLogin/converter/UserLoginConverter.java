package com.caboolo.backend.userLogin.converter;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.domain.UserLogin;

/**
 * Converts UserLogin domain entities to their corresponding DTOs.
 */
public final class UserLoginConverter {

    private UserLoginConverter() {
        // utility class
    }

    /**
     * Converts a UserLogin domain entity to an AuthResponse DTO.
     */
    public static AuthResponse toAuthResponse(UserLogin userLogin) {

        return AuthResponse.Builder.authResponse()
                .withFirebaseUid(userLogin.getFirebaseUid())
                .withMessage("Login successful")
                .withPhoneNumber(userLogin.getPhoneNumber())
                .build();
    }
}
