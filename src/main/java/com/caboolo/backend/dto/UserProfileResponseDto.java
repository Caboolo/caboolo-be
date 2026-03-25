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
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto extends GenericEntityDto {

    private Long id;

    private String firebaseUid;

    private String phoneNumber;

    private String name;

    private String email;

    private String imageUrl;

    public UserProfileResponseDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, Long id,
                                  String firebaseUid, String phoneNumber, String name, String email, String imageUrl) {
        super(dateCreated, lastModified, isDeleted);
        this.id = id;
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
        IdStep withIsDeleted(boolean isDeleted);
    }

    public static interface IdStep {
        FirebaseUidStep withId(Long id);
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
        UserProfileResponseDto build();
    }

    public static class Builder
        implements DateCreatedStep, LastModifiedStep, IsDeletedStep, IdStep, FirebaseUidStep, PhoneNumberStep, NameStep,
        EmailStep, ImageUrlStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private Long id;
        private String firebaseUid;
        private String phoneNumber;
        private String name;
        private String email;
        private String imageUrl;

        private Builder() {
        }

        public static DateCreatedStep userProfileResponseDto() {
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
        public FirebaseUidStep withId(Long id) {
            this.id = id;
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
        public UserProfileResponseDto build() {
            return new UserProfileResponseDto(
                this.dateCreated,
                this.lastModified,
                this.isDeleted,
                this.id,
                this.firebaseUid,
                this.phoneNumber,
                this.name,
                this.email,
                this.imageUrl
            );
        }
    }
}
