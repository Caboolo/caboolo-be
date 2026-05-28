package com.caboolo.backend.notification.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class NotificationResponseDto {

    private final String notificationId;
    private final String title;
    private final String body;
    private final String type;
    private final String rideId;
    private final String senderUserId;
    private final boolean isRead;
    private final LocalDateTime createdAt;
    private final Map<String, String> metadata;

    private NotificationResponseDto(Builder builder) {
        this.notificationId = builder.notificationId;
        this.title = builder.title;
        this.body = builder.body;
        this.type = builder.type;
        this.rideId = builder.rideId;
        this.senderUserId = builder.senderUserId;
        this.isRead = builder.isRead;
        this.createdAt = builder.createdAt;
        this.metadata = builder.metadata;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step Builder
    // ─────────────────────────────────────────────────────────────────────────

    public interface NotificationIdStep {
        TitleStep withNotificationId(String notificationId);
    }

    public interface TitleStep {
        BodyStep withTitle(String title);
    }

    public interface BodyStep {
        TypeStep withBody(String body);
    }

    public interface TypeStep {
        BuildStep withType(String type);
    }

    public interface BuildStep {
        BuildStep withRideId(String rideId);
        BuildStep withSenderUserId(String senderUserId);
        BuildStep withIsRead(boolean isRead);
        BuildStep withCreatedAt(LocalDateTime createdAt);
        BuildStep withMetadata(Map<String, String> metadata);
        NotificationResponseDto build();
    }

    public static class Builder implements NotificationIdStep, TitleStep, BodyStep, TypeStep, BuildStep {

        private String notificationId;
        private String title;
        private String body;
        private String type;
        private String rideId;
        private String senderUserId;
        private boolean isRead;
        private LocalDateTime createdAt;
        private Map<String, String> metadata;

        private Builder() {
        }

        public static NotificationIdStep notificationResponse() {
            return new Builder();
        }

        @Override
        public TitleStep withNotificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        @Override
        public BodyStep withTitle(String title) {
            this.title = title;
            return this;
        }

        @Override
        public TypeStep withBody(String body) {
            this.body = body;
            return this;
        }

        @Override
        public BuildStep withType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public BuildStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public BuildStep withSenderUserId(String senderUserId) {
            this.senderUserId = senderUserId;
            return this;
        }

        @Override
        public BuildStep withIsRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        @Override
        public BuildStep withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Override
        public BuildStep withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public NotificationResponseDto build() {
            return new NotificationResponseDto(this);
        }
    }
}
