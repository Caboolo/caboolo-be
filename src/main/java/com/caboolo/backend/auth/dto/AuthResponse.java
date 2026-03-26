package com.caboolo.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String firebaseUid;
    private String message;
    private String phoneNumber;

    public static interface FirebaseUidStep {
        MessageStep withFirebaseUid(String firebaseUid);
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


    public static class Builder implements FirebaseUidStep, MessageStep, PhoneNumberStep, BuildStep {
        private String firebaseUid;
        private String message;
        private String phoneNumber;

        private Builder() {
        }

        public static FirebaseUidStep authResponse() {
            return new Builder();
        }

        @Override
        public MessageStep withFirebaseUid(String firebaseUid) {
            this.firebaseUid = firebaseUid;
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
                    this.firebaseUid,
                    this.message,
                    this.phoneNumber
            );
        }
    }
}
