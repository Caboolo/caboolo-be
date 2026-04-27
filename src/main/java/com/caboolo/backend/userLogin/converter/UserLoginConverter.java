package com.caboolo.backend.userLogin.converter;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.dto.UserLoginDto;

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
                .withUserId(userLogin.getUserId())
                .withMessage("Login successful")
                .withPhoneNumber(userLogin.getPhoneNumber())
                .build();
    }

    /**
     * Converts a UserLogin domain entity to a UserLoginDto.
     */
    public static UserLoginDto toUserLoginDto(UserLogin userLogin) {
        return UserLoginDto.Builder.userLoginDto()
                .withUserId(userLogin.getUserId())
                .withPhoneNumber(userLogin.getPhoneNumber())
                .withDateCreated(userLogin.getDateCreated())
                .withLastModified(userLogin.getLastModified())
                .withIsDeleted(userLogin.isDeleted())
                .build();
    }
}
