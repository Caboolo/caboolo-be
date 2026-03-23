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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(length = 1000)
    private String imageUrl;

    @Column(unique = true)
    private String email;

    @Column
    private String photoPublicId;

    public UserDetails(String name, Long userId, Gender gender, String imageUrl, String email, String photoPublicId) {
        this.name = name;
        this.userId = userId;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.email = email;
        this.photoPublicId = photoPublicId;
    }
}
