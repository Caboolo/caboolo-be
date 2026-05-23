package com.caboolo.backend.notification.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.notification.enums.DeviceType;
import com.caboolo.backend.notification.enums.FcmTokenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_fcm_token",
    indexes = {
        @Index(name = "idx_user_id_status", columnList = "user_id, status"),
        @Index(name = "idx_fcm_token", columnList = "fcm_token", unique = true),
        @Index(name = "idx_device_id", columnList = "device_id")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmToken extends GenericIdEntity {

    @Column(name = "user_fcm_token_id", nullable = false, unique = true)
    private String userFcmTokenId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FcmTokenStatus status;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "app_version", length = 50)
    private String appVersion;

    public static interface UserFcmTokenIdStep {
        UserIdStep withUserFcmTokenId(String userFcmTokenId);
    }

    public static interface UserIdStep {
        DeviceIdStep withUserId(String userId);
    }

    public static interface DeviceIdStep {
        FcmTokenStep withDeviceId(String deviceId);
    }

    public static interface FcmTokenStep {
        DeviceTypeStep withFcmToken(String fcmToken);
    }

    public static interface DeviceTypeStep {
        StatusStep withDeviceType(DeviceType deviceType);
    }

    public static interface StatusStep {
        BuildStep withStatus(FcmTokenStatus status);
    }

    public static interface BuildStep {
        BuildStep withLastUsedAt(LocalDateTime lastUsedAt);
        BuildStep withAppVersion(String appVersion);
        UserFcmToken build();
    }

    public static class Builder implements UserFcmTokenIdStep, UserIdStep, DeviceIdStep, FcmTokenStep, DeviceTypeStep, StatusStep, BuildStep {
        private String userFcmTokenId;
        private String userId;
        private String deviceId;
        private String fcmToken;
        private DeviceType deviceType;
        private FcmTokenStatus status;
        private LocalDateTime lastUsedAt;
        private String appVersion;

        private Builder() {}

        public static UserFcmTokenIdStep userFcmToken() {
            return new Builder();
        }

        @Override
        public UserIdStep withUserFcmTokenId(String userFcmTokenId) {
            this.userFcmTokenId = userFcmTokenId;
            return this;
        }

        @Override
        public DeviceIdStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public FcmTokenStep withDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        @Override
        public DeviceTypeStep withFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
            return this;
        }

        @Override
        public StatusStep withDeviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        @Override
        public BuildStep withStatus(FcmTokenStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public BuildStep withLastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        @Override
        public BuildStep withAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        @Override
        public UserFcmToken build() {
            return new UserFcmToken(
                    this.userFcmTokenId,
                    this.userId,
                    this.deviceId,
                    this.fcmToken,
                    this.deviceType,
                    this.status,
                    this.lastUsedAt,
                    this.appVersion
            );
        }
    }
}
