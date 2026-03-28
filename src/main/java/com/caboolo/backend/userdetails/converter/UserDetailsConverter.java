package com.caboolo.backend.userdetails.converter;

import com.caboolo.backend.dto.UserDetailResponseDto;
import com.caboolo.backend.userLogin.domain.UserLogin;
import com.caboolo.backend.userdetails.domain.UserDetails;

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
                .withUserDetailId(details.getUserDetailsId())
                .withFirebaseUid(details.getUserId())
                .withPhoneNumber(details.getPhoneNumber())
                .withName(details.getName())
                .withEmail(details.getEmail())
                .withImageUrl(details.getImageUrl())
                .build();
    }

    /**
     * Converts a UserLogin + UserDetails pair into a full UserDetailResponse.
     */
    public static UserDetailResponseDto toProfileResponse(UserLogin userLogin, UserDetails details) {
        return UserDetailResponseDto.Builder.userDetailResponseDto()
                .withDateCreated(userLogin.getDateCreated())
                .withLastModified(userLogin.getLastModified())
                .withIsDeleted(false)
                .withUserDetailId(details.getUserDetailsId())
                .withFirebaseUid(userLogin.getFirebaseUid())
                .withPhoneNumber(userLogin.getPhoneNumber())
                .withName(details.getName())
                .withEmail(details.getEmail())
                .withImageUrl(details.getImageUrl())
                .build();
    }
}
