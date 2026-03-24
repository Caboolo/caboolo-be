package com.caboolo.backend.userdetails.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.caboolo.backend.core.domain.GenericIdEntity;

@Entity
@Table(name = "user_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    public static interface NameStep {
        UserIdStep withName(String name);
    }

    public static interface UserIdStep {
        GenderStep withUserId(Long userId);
    }

    public static interface GenderStep {
        ImageUrlStep withGender(Gender gender);
    }

    public static interface ImageUrlStep {
        EmailStep withImageUrl(String imageUrl);
    }

    public static interface EmailStep {
        PhoneNumberStep withEmail(String email);
    }

    public static interface PhoneNumberStep {
        PhotoPublicIdStep withPhoneNumber(String phoneNumber);
    }

    public static interface PhotoPublicIdStep {
        BuildStep withPhotoPublicId(String photoPublicId);
    }

    public static interface BuildStep {
        UserDetails build();
    }

    public static class Builder implements NameStep, UserIdStep, GenderStep, ImageUrlStep, EmailStep, PhoneNumberStep, PhotoPublicIdStep, BuildStep {
        private String name;
        private Long userId;
        private Gender gender;
        private String imageUrl;
        private String email;
        private String phoneNumber;
        private String photoPublicId;

        private Builder() {
        }

        public static NameStep userDetails() {
            return new Builder();
        }

        @Override
        public UserIdStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public GenderStep withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public ImageUrlStep withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        @Override
        public EmailStep withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        @Override
        public PhoneNumberStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public PhotoPublicIdStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public BuildStep withPhotoPublicId(String photoPublicId) {
            this.photoPublicId = photoPublicId;
            return this;
        }

        @Override
        public UserDetails build() {
            return new UserDetails(
                    this.name,
                    this.userId,
                    this.gender,
                    this.imageUrl,
                    this.email,
                    this.phoneNumber,
                    this.photoPublicId
            );
        }
    }
}
