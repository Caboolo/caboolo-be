package com.caboolo.backend.userdetails.converter;

import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsConverter {

    public UserDetailResponseDto convertToResponseDto(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return new UserDetailResponseDto(
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getUserId(),
                userDetails.getGender(),
                userDetails.getImageUrl()
        );
    }
}
