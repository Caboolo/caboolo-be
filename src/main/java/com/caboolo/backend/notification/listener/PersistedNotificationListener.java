package com.caboolo.backend.notification.listener;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.event.RideNotificationEvent;
import com.caboolo.backend.notification.service.NotificationService;
import com.caboolo.backend.userdetails.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listens for {@link RideNotificationEvent}s and saves notifications to the database.
 * <p>
 * Runs AFTER_COMMIT so notifications are only saved when the related transaction succeeds.
 * Runs @Async so it never blocks the calling thread.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistedNotificationListener {

    private final NotificationService notificationService;
    private final UserDetailService userDetailService;
    private final SequenceGenerator sequenceGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRideNotification(RideNotificationEvent event) {
        try {
            if (event.getType().shouldPersist()) {
                persistNotifications(event);
            } else {
                log.warn("Unknown ride notification type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Failed to persist ride notification [type={}, rideId={}]: {}",
                    event.getType(), event.getRideId(), e.getMessage());
        }
    }


    private void persistNotifications(RideNotificationEvent event) {
        if (event.getRecipientUserIds() == null || event.getRecipientUserIds().isEmpty()) {
            return;
        }

        String body = event.getBody();
        if (body != null && body.contains("%s")) {
            String actorName = resolveUserName(event.getSenderUserId(), "Someone");
            body = String.format(body, actorName);
        }

        Map<String, String> metadata = buildMetadataMap(event);

        List<Notification> notifications = new ArrayList<>();
        for (String recipientId : event.getRecipientUserIds()) {
            Notification notification = Notification.Builder.notification()
                    .withNotificationId(sequenceGenerator.nextId())
                    .withUserId(recipientId)
                    .withTitle(event.getTitle())
                    .withBody(body)
                    .withType(event.getType().name())
                    .withSenderUserId(event.getSenderUserId())
                    .withRideId(event.getRideId())
                    .withIsRead(false)
                    .withMetadata(metadata)
                    .build();
            notifications.add(notification);
        }

        notificationService.saveInAppNotifications(notifications);
    }

    /**
     * Builds the same data map that {@link RideNotificationListener} sends via FCM.
     * The {@link com.caboolo.backend.core.converter.StringMapConverter} handles
     * JSON serialization to the DB column transparently.
     */
    private Map<String, String> buildMetadataMap(RideNotificationEvent event) {
        Map<String, String> meta = new HashMap<>();
        meta.put("type", event.getType().name());
        meta.put("screen", event.getType().getScreen());
        if (event.getRideId() != null) {
            meta.put("rideId", event.getRideId());
        }
        switch (event.getType()) {
            case RIDE_REQUEST_SENT, MATCH_FOUND -> {
                if (event.getSenderUserId() != null) {
                    meta.put("requesterId", event.getSenderUserId());
                }
            }
            case MEMBER_LEFT -> {
                if (event.getSenderUserId() != null) {
                    meta.put("userId", event.getSenderUserId());
                }
            }
            default -> { /* no extra fields */ }
        }
        return meta;
    }

    private String resolveUserName(String userId, String fallback) {
        if (userId == null) return fallback;
        try {
            String user = userDetailService.getNameByUserId(userId);
            return user != null ? user: fallback;
        } catch (Exception e) {
            log.warn("Could not resolve user name for persisting notification {}: {}", userId, e.getMessage());
            return fallback;
        }
    }
}
