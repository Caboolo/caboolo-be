package com.caboolo.backend.userLogin.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import com.caboolo.backend.core.domain.GenericIdEntity;

@Entity
@Table(name = "user_login")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserLogin extends GenericIdEntity {

    @Column(name = "user_login_id")
    private Long userLoginId;

    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    public static interface UserLoginIdStep {
        FirebaseUidStep withUserLoginId(Long userLoginId);
    }

    public static interface FirebaseUidStep {
        PhoneNumberStep withFirebaseUid(String firebaseUid);
    }

    public static interface PhoneNumberStep {
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        UserLogin build();
    }

    public static class Builder implements UserLoginIdStep, FirebaseUidStep, PhoneNumberStep, BuildStep {
        private Long userLoginId;
        private String firebaseUid;
        private String phoneNumber;

        private Builder() {
        }

        public static UserLoginIdStep userLogin() {
            return new Builder();
        }

        @Override
        public FirebaseUidStep withUserLoginId(Long userLoginId) {
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
        public UserLogin build() {
            return new UserLogin(
                    this.userLoginId,
                    this.firebaseUid,
                    this.phoneNumber
            );
        }
    }
}
