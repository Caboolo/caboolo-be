package com.caboolo.backend.userLogin.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto extends GenericEntityDto {

    /** Internal sequence-generated identifier for this login record. */
    private String userLoginId;

    /** Firebase UID — acts as the external userId for API consumers. */
    private String firebaseUid;

    private String phoneNumber;

    // -------------------------------------------------------------------------
    // Step Builder
    // -------------------------------------------------------------------------

    public static interface UserLoginIdStep {
        FirebaseUidStep withUserLoginId(String userLoginId);
    }

    public static interface FirebaseUidStep {
        PhoneNumberStep withFirebaseUid(String firebaseUid);
    }

    public static interface PhoneNumberStep {
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        BuildStep withDateCreated(LocalDateTime dateCreated);
        BuildStep withLastModified(LocalDateTime lastModified);
        BuildStep withIsDeleted(boolean isDeleted);
        UserLoginDto build();
    }

    public static class Builder
            implements UserLoginIdStep, FirebaseUidStep, PhoneNumberStep, BuildStep {

        private String userLoginId;
        private String firebaseUid;
        private String phoneNumber;
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;

        private Builder() {}

        public static UserLoginIdStep userLoginDto() {
            return new Builder();
        }

        @Override
        public FirebaseUidStep withUserLoginId(String userLoginId) {
            this.userLoginId = userLoginId;
            return this;
        }

        @Override
        public PhoneNumberStep withFirebaseUid(String firebaseUid) {
            this.firebaseUid = firebaseUid;
            return this;
        }

        @Override
        public BuildStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public BuildStep withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        @Override
        public BuildStep withLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        @Override
        public BuildStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public UserLoginDto build() {
            UserLoginDto dto = new UserLoginDto(userLoginId, firebaseUid, phoneNumber);
            dto.setDateCreated(dateCreated);
            dto.setLastModified(lastModified);
            dto.setDeleted(isDeleted);
            return dto;
        }
    }
}
