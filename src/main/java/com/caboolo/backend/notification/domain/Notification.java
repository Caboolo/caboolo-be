package com.caboolo.backend.notification.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends GenericIdEntity {

    @Column(name = "notification_id", nullable = false, unique = true)
    private String notificationId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "sender_user_id")
    private String senderUserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "ride_id")
    private String rideId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public static interface NotificationIdStep {
        UserIdStep withNotificationId(String notificationId);
    }

    public static interface UserIdStep {
        TitleStep withUserId(String userId);
    }

    public static interface TitleStep {
        BodyStep withTitle(String title);
    }

    public static interface BodyStep {
        TypeStep withBody(String body);
    }

    public static interface TypeStep {
        BuildStep withType(String type);
    }

    public static interface BuildStep {
        BuildStep withSenderUserId(String senderUserId);
        BuildStep withRideId(String rideId);
        BuildStep withIsRead(boolean isRead);
        Notification build();
    }

    public static class Builder implements NotificationIdStep, UserIdStep, TitleStep, BodyStep, TypeStep, BuildStep {
        private String notificationId;
        private String userId;
        private String senderUserId;
        private String title;
        private String body;
        private String type;
        private String rideId;
        private boolean isRead = false;

        private Builder() {
        }

        public static NotificationIdStep notification() {
            return new Builder();
        }

        @Override
        public UserIdStep withNotificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        @Override
        public TitleStep withUserId(String userId) {
            this.userId = userId;
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
        public BuildStep withSenderUserId(String senderUserId) {
            this.senderUserId = senderUserId;
            return this;
        }

        @Override
        public BuildStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public BuildStep withIsRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        @Override
        public Notification build() {
            return new Notification(
                    this.notificationId,
                    this.userId,
                    this.senderUserId,
                    this.title,
                    this.body,
                    this.type,
                    this.rideId,
                    this.isRead
            );
        }
    }
}
