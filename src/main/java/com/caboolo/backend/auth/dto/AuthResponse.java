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

    public static interface UserIdStep {
        MessageStep withUserId(String userId);
    }

    public static interface MessageStep {
        PhoneNumberStep withMessage(String message);
    }

    public static interface PhoneNumberStep {
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        AuthResponse build();
    }


    public static class Builder implements UserIdStep, MessageStep, PhoneNumberStep, BuildStep {
        private String userId;
        private String message;
        private String phoneNumber;

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
        public BuildStep withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        @Override
        public AuthResponse build() {
            return new AuthResponse(
                    this.userId,
                    this.message,
                    this.phoneNumber
            );
        }
    }
}
