package com.caboolo.backend.userdetails.converter;

import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import org.springframework.stereotype.Component;

/**
 * Converts UserDetails domain entities to their corresponding DTOs.
 */
@Component
public final class UserDetailsConverter {

    private UserDetailsConverter() {
        // utility class
    }

    /**
     * Converts a UserDetails domain entity to a UserDetailResponseDto.
     */
    public UserDetailResponseDto toDetailResponseDto(UserDetail details) {
        if (details == null) {
            return null;
        }
        return UserDetailResponseDto.Builder.userDetailResponseDto()
                .withDateCreated(details.getDateCreated())
                .withLastModified(details.getLastModified())
                .withIsDeleted(details.isDeleted())
                .withName(details.getName())
                .withUserId(details.getUserId())
                .withGender(details.getGender())
                .withImageUrl(details.getImageUrl())
                .withEmail(details.getEmail())
                .withPhoneNumber(details.getPhoneNumber())
                .build();
    }
}
