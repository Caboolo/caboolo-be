package com.caboolo.backend.notification.converter;

import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.dto.NotificationResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Converts Notification domain entities to their corresponding DTOs.
 */
@Slf4j
@Component
public final class NotificationConverter {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public NotificationConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a Notification domain entity to a NotificationResponseDto.
     */
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
                .withMetadata(parseMetadata(notification.getMetadata()))
                .build();
    }

    /**
     * Safely deserializes the metadata JSON string into a Map.
     * Returns null if the string is blank or cannot be parsed.
     */
    private Map<String, String> parseMetadata(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse notification metadata JSON: {}", e.getMessage());
            return null;
        }
    }
}
