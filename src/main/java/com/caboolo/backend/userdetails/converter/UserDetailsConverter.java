package com.caboolo.backend.userdetails.converter;

import com.caboolo.backend.dto.UserProfileResponseDto;
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
        return UserDetailResponseDto.Builder.userDetailResponseDto()
                .withDateCreated(details.getDateCreated())
                .withLastModified(details.getLastModified())
                .withIsDeleted(details.isDeleted())
                .withId(details.getUserDetailsId())
                .withName(details.getName())
                .withUserId(details.getUserId())
                .withGender(details.getGender())
                .withImageUrl(details.getImageUrl())
                .withEmail(details.getEmail())
                .withPhoneNumber(details.getPhoneNumber())
                .build();
    }

    /**
     * Converts a UserLogin + UserDetails pair into a full UserProfileResponse.
     */
    public static UserProfileResponseDto toProfileResponse(UserLogin userLogin, UserDetails details) {
        return UserProfileResponseDto.Builder.userProfileResponseDto()
                .withDateCreated(userLogin.getDateCreated())
                .withLastModified(userLogin.getLastModified())
                .withIsDeleted(false)
                .withId(userLogin.getUserLoginId())
                .withFirebaseUid(userLogin.getFirebaseUid())
                .withPhoneNumber(userLogin.getPhoneNumber())
                .withName(details.getName())
                .withEmail(details.getEmail())
                .withImageUrl(details.getImageUrl())
                .build();
    }
}
