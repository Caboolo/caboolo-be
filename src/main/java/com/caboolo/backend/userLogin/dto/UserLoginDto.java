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

    /** Internal sequence-generated identifier for this user. */
    private String userId;

    private String userLoginId;

    private String phoneNumber;

    public UserLoginDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, String userId, String userLoginId, String phoneNumber) {
        super(dateCreated, lastModified, isDeleted);
        this.userId = userId;
        this.userLoginId = userLoginId;
        this.phoneNumber = phoneNumber;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        UserIdStep withIsDeleted(boolean isDeleted);
    }

    public static interface UserIdStep {
        UserLoginIdStep withUserId(String userId);
    }

    public static interface UserLoginIdStep {
        PhoneNumberStep withUserLoginId(String userLoginId);
    }

    public static interface PhoneNumberStep {
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        UserLoginDto build();
    }


    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, UserIdStep, UserLoginIdStep, PhoneNumberStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private String userId;
        private String userLoginId;
        private String phoneNumber;

        private Builder() {
        }

        public static DateCreatedStep userLoginDto() {
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
        public UserIdStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public UserLoginIdStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public PhoneNumberStep withUserLoginId(String userLoginId) {
            this.userLoginId = userLoginId;
            return this;
        }

        @Override
        public BuildStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public UserLoginDto build() {
            return new UserLoginDto(
                    this.dateCreated,
                    this.lastModified,
                    this.isDeleted,
                    this.userId,
                    this.userLoginId,
                    this.phoneNumber
            );
        }
    }
}
