package com.caboolo.backend.userdetails.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.caboolo.backend.core.domain.GenericIdEntity;

@Entity
@Table(name = "user_detail")
@Data
@NoArgsConstructor
public class UserDetails extends GenericIdEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "photo_public_id")
    private String photoPublicId;

    public UserDetails(String name, Long userId, Gender gender, String imageUrl, String email, String phoneNumber, String photoPublicId) {
        this.name = name;
        this.userId = userId;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoPublicId = photoPublicId;
    }
}
