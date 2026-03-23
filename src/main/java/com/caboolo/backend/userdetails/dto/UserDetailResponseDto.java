package com.caboolo.backend.userdetails.dto;

import com.caboolo.backend.userdetails.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto {
    private Long id;
    private String name;
    private Long userId;
    private Gender gender;
    private String imageUrl;
}
