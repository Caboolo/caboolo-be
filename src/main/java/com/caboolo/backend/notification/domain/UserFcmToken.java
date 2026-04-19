package com.caboolo.backend.notification.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_fcm_token",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fcm_token"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmToken extends GenericIdEntity {

    @Column(name = "user_fcm_token_id", nullable = false)
    private String userFcmTokenId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    public static interface UserFcmTokenIdStep {
        UserIdStep withUserFcmTokenId(String userFcmTokenId);
    }

    public static interface UserIdStep {
        FcmTokenStep withUserId(String userId);
    }

    public static interface FcmTokenStep {
        BuildStep withFcmToken(String fcmToken);
    }

    public static interface BuildStep {
        UserFcmToken build();
    }

    public static class Builder implements UserFcmTokenIdStep, UserIdStep, FcmTokenStep, BuildStep {
        private String userFcmTokenId;
        private String userId;
        private String fcmToken;

        private Builder() {
        }

        public static UserFcmTokenIdStep userFcmToken() {
            return new Builder();
        }

        @Override
        public UserIdStep withUserFcmTokenId(String userFcmTokenId) {
            this.userFcmTokenId = userFcmTokenId;
            return this;
        }

        @Override
        public FcmTokenStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public BuildStep withFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
            return this;
        }

        @Override
        public UserFcmToken build() {
            return new UserFcmToken(
                    this.userFcmTokenId,
                    this.userId,
                    this.fcmToken
            );
        }
    }
}
