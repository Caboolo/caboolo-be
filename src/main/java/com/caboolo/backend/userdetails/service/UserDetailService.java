package com.caboolo.backend.userdetails.service;

import com.caboolo.backend.userdetails.domain.UserDetails;
import com.caboolo.backend.userdetails.dto.UserDetailRequestDto;
import com.caboolo.backend.userdetails.repository.UserDetailRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailService {

    private final UserDetailRepository userDetailRepository;

    public UserDetailService(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    public UserDetails saveOrUpdateUserDetails(UserDetailRequestDto requestDto) {
        if (requestDto.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        Optional<UserDetails> existingDetailsOpt = userDetailRepository.findByUserId(requestDto.getUserId());
        UserDetails details;

        if (existingDetailsOpt.isPresent()) {
            details = existingDetailsOpt.get();
            details.setName(requestDto.getName());
            details.setGender(requestDto.getGender());
            details.setImageUrl(requestDto.getImageUrl());
        } else {
            details = new UserDetails(
                    requestDto.getName(),
                    requestDto.getUserId(),
                    requestDto.getGender(),
                    requestDto.getImageUrl()
            );
        }

        return userDetailRepository.save(details);
    }
}
