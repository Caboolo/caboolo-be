package com.caboolo.backend.userdetails.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import java.time.LocalDateTime;
import com.caboolo.backend.userdetails.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto extends GenericEntityDto {
    private Long id;
    private String name;
    private Long userId;
    private Gender gender;
    private String imageUrl;
    private String email;
    private String phoneNumber;

    public UserDetailResponseDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, Long id, String name, Long userId, Gender gender, String imageUrl, String email, String phoneNumber) {
        super(dateCreated, lastModified, isDeleted);
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        IdStep withIsDeleted(boolean isDeleted);
    }

    public static interface IdStep {
        NameStep withId(Long id);
    }

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
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        UserDetailResponseDto build();
    }

    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, IdStep, NameStep, UserIdStep, GenderStep, ImageUrlStep, EmailStep, PhoneNumberStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private Long id;
        private String name;
        private Long userId;
        private Gender gender;
        private String imageUrl;
        private String email;
        private String phoneNumber;

        private Builder() {}

        public static DateCreatedStep userDetailResponseDto() {
            return new Builder();
        }

        @Override
        public LastModifiedStep withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        @Override
        public IsDeletedStep withLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        @Override
        public IdStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public NameStep withId(Long id) {
            this.id = id;
            return this;
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
        public BuildStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public UserDetailResponseDto build() {
            return new UserDetailResponseDto(
                this.dateCreated,
                this.lastModified,
                this.isDeleted,
                this.id,
                this.name,
                this.userId,
                this.gender,
                this.imageUrl,
                this.email,
                this.phoneNumber
            );
        }
    }
}
