package com.caboolo.backend.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload carrying the user's profile information.
 */

@Data
@NoArgsConstructor
public class UserDetailResponseDto extends GenericEntityDto {

    private Long userDetailId;

    private String firebaseUid;

    private String phoneNumber;

    private String name;

    private String email;

    private String imageUrl;

    public UserDetailResponseDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, Long userDetailId, String firebaseUid, String phoneNumber, String name, String email, String imageUrl) {
        super(dateCreated, lastModified, isDeleted);
        this.userDetailId = userDetailId;
        this.firebaseUid = firebaseUid;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        UserDetailIdStep withIsDeleted(boolean isDeleted);
    }

    public static interface UserDetailIdStep {
        FirebaseUidStep withUserDetailId(Long userDetailId);
    }

    public static interface FirebaseUidStep {
        PhoneNumberStep withFirebaseUid(String firebaseUid);
    }

    public static interface PhoneNumberStep {
        NameStep withPhoneNumber(String phoneNumber);
    }

    public static interface NameStep {
        EmailStep withName(String name);
    }

    public static interface EmailStep {
        ImageUrlStep withEmail(String email);
    }

    public static interface ImageUrlStep {
        BuildStep withImageUrl(String imageUrl);
    }

    public static interface BuildStep {
        UserDetailResponseDto build();
    }


    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, UserDetailIdStep, FirebaseUidStep, PhoneNumberStep, NameStep, EmailStep, ImageUrlStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private Long userDetailId;
        private String firebaseUid;
        private String phoneNumber;
        private String name;
        private String email;
        private String imageUrl;

        private Builder() {
        }

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
        public UserDetailIdStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public FirebaseUidStep withUserDetailId(Long userDetailId) {
            this.userDetailId = userDetailId;
            return this;
        }

        @Override
        public PhoneNumberStep withFirebaseUid(String firebaseUid) {
            this.firebaseUid = firebaseUid;
            return this;
        }

        @Override
        public NameStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public EmailStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ImageUrlStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public BuildStep withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        @Override
        public UserDetailResponseDto build() {
            return new UserDetailResponseDto(
                    this.dateCreated,
                    this.lastModified,
                    this.isDeleted,
                    this.userDetailId,
                    this.firebaseUid,
                    this.phoneNumber,
                    this.name,
                    this.email,
                    this.imageUrl
            );
        }
    }
}
