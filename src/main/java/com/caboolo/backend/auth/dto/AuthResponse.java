package com.caboolo.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String userId;
    private String message;
    private String phoneNumber;
    private boolean isProfileCreated;
    private String accessToken;
    private String refreshToken;

    public static interface UserIdStep {
        MessageStep withUserId(String userId);
    }

    public static interface MessageStep {
        PhoneNumberStep withMessage(String message);
    }

    public static interface PhoneNumberStep {
        IsProfileCreatedStep withPhoneNumber(String phoneNumber);
    }

    public static interface IsProfileCreatedStep {
        AccessTokenStep withIsProfileCreated(boolean isProfileCreated);
    }

    public static interface AccessTokenStep {
        RefreshTokenStep withAccessToken(String accessToken);
    }

    public static interface RefreshTokenStep {
        BuildStep withRefreshToken(String refreshToken);
    }

    public static interface BuildStep {
        AuthResponse build();
    }

    public static class Builder implements UserIdStep, MessageStep, PhoneNumberStep, IsProfileCreatedStep, AccessTokenStep, RefreshTokenStep, BuildStep {
        private String userId;
        private String message;
        private String phoneNumber;
        private boolean isProfileCreated;
        private String accessToken;
        private String refreshToken;

        private Builder() {
        }

        public static UserIdStep authResponse() {
            return new Builder();
        }

        @Override
        public MessageStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public PhoneNumberStep withMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public IsProfileCreatedStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public AccessTokenStep withIsProfileCreated(boolean isProfileCreated) {
            this.isProfileCreated = isProfileCreated;
            return this;
        }

        @Override
        public RefreshTokenStep withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        @Override
        public BuildStep withRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        @Override
        public AuthResponse build() {
            return new AuthResponse(
                this.userId,
                this.message,
                this.phoneNumber,
                this.isProfileCreated,
                this.accessToken,
                this.refreshToken
            );
        }
    }
}
