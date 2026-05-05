package com.caboolo.backend.userLogin.converter;

import com.caboolo.backend.auth.dto.AuthResponse;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userLogin.dto.UserLoginDto;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converts UserLogin domain entities to their corresponding DTOs.
 */
@Component
public class UserLoginConverter {

    private final UserDetailService userDetailService;

    public UserLoginConverter(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    /**
     * Converts a UserLogin domain entity to an AuthResponse DTO.
     */
    public AuthResponse toAuthResponse(UserLogin userLogin) {
        return AuthResponse.Builder.authResponse()
            .withUserId(userLogin.getUserId())
            .withMessage("Login successful")
            .withPhoneNumber(userLogin.getPhoneNumber())
            .withIsProfileCreated(userDetailService.existsByPhoneNumber(userLogin.getPhoneNumber()))
            .build();
    }

    /**
     * Converts a UserLogin domain entity to a UserLoginDto.
     */
    public UserLoginDto toUserLoginDto(UserLogin userLogin) {
        return UserLoginDto.Builder.userLoginDto()
            .withDateCreated(userLogin.getDateCreated())
            .withLastModified(userLogin.getLastModified())
            .withIsDeleted(userLogin.isDeleted())
            .withUserId(userLogin.getUserId())
            .withUserLoginId(userLogin.getUserLoginId())
            .withPhoneNumber(userLogin.getPhoneNumber())
            .build();
    }
}
