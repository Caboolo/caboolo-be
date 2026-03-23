package com.caboolo.backend.userdetails.converter;

import com.caboolo.backend.dto.UserProfileResponse;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;

/**
 * Converts UserDetails domain entities to their corresponding DTOs.
 */
public final class UserDetailsConverter {

    private UserDetailsConverter() {
        // utility class
    }

    /**
     * Converts a UserDetails domain entity to a UserDetailResponseDto.
     */
    public static UserDetailResponseDto toDetailResponseDto(UserDetails details) {
        if (details == null) {
            return null;
        }
        return new UserDetailResponseDto(
                details.getId(),
                details.getName(),
                details.getUserId(),
                details.getGender(),
                details.getImageUrl(),
                details.getEmail(),
                details.getPhoneNumber()
        );
    }

    /**
     * Converts a UserLogin + UserDetails pair into a full UserProfileResponse.
     */
    public static UserProfileResponse toProfileResponse(UserLogin userLogin, UserDetails details) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(userLogin.getId());
        resp.setFirebaseUid(userLogin.getFirebaseUid());
        resp.setPhoneNumber(userLogin.getPhoneNumber());
        resp.setName(details.getName());
        resp.setEmail(details.getEmail());
        resp.setImageUrl(details.getImageUrl());
        resp.setDateCreated(userLogin.getDateCreated());
        resp.setLastModified(userLogin.getLastModified());
        return resp;
    }
}
