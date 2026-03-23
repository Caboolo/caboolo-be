package com.caboolo.backend.userdetails.dto;

import com.caboolo.backend.userdetails.domain.Gender;

public class UserDetailRequestDto {
    private String name;
    private Long userId;
    private Gender gender;
    private String imageUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
