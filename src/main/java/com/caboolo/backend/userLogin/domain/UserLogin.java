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
    private String userLoginId;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    public static interface UserLoginIdStep {
        UserIdStep withUserLoginId(String userLoginId);
    }

    public static interface UserIdStep {
        PhoneNumberStep withUserId(String userId);
    }

    public static interface PhoneNumberStep {
        BuildStep withPhoneNumber(String phoneNumber);
    }

    public static interface BuildStep {
        UserLogin build();
    }


    public static class Builder implements UserLoginIdStep, UserIdStep, PhoneNumberStep, BuildStep {
        private String userLoginId;
        private String userId;
        private String phoneNumber;

        private Builder() {
        }

        public static UserLoginIdStep userLogin() {
            return new Builder();
        }

        @Override
        public UserIdStep withUserLoginId(String userLoginId) {
            this.userLoginId = userLoginId;
            return this;
        }

        @Override
        public PhoneNumberStep withUserId(String userId) {
            this.userId = userId;
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
                    this.userId,
                    this.phoneNumber
            );
        }
    }
}
