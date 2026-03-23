package com.caboolo.backend.userdetails.dto;

import com.caboolo.backend.userdetails.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailRequestDto {
    private String name;
    private Long userId;
    private Gender gender;
    private String imageUrl;
}
