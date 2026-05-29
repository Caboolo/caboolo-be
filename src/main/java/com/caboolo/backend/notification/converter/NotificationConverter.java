package com.caboolo.backend.notification.converter;

import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.dto.NotificationResponseDto;
import org.springframework.stereotype.Component;

/**
 * Converts Notification domain entities to their corresponding DTOs.
 */
@Component
public final class NotificationConverter {

    public NotificationResponseDto toResponseDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponseDto.Builder.notificationResponse()
                .withNotificationId(notification.getNotificationId())
                .withTitle(notification.getTitle())
                .withBody(notification.getBody())
                .withType(notification.getType())
                .withRideId(notification.getRideId())
                .withSenderUserId(notification.getSenderUserId())
                .withIsRead(notification.isRead())
                .withCreatedAt(notification.getDateCreated())
                .withMetadata(notification.getMetadata())
                .build();
    }
}
