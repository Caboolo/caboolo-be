package com.caboolo.backend.notification.listener;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.domain.Notification;
import com.caboolo.backend.notification.event.RideNotificationEvent;
import com.caboolo.backend.notification.service.NotificationService;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.ArrayList;
import java.util.List;

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
            switch (event.getType()) {
                case RIDE_REQUEST_SENT -> handleRideRequestSent(event);
                case RIDE_CONFIRMED -> handleRideConfirmed(event);
                case MATCH_FOUND -> handleMatchFound(event);
                case MEMBER_LEFT -> handleMemberLeft(event);
                default -> log.warn("Unknown ride notification type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Failed to persist ride notification [type={}, rideId={}]: {}",
                    event.getType(), event.getRideId(), e.getMessage());
        }
    }

    private void handleRideRequestSent(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getSenderUserId(), "Someone");
        String title = "New Join Request";
        String body = actorName + " wants to join your ride";
        persistNotifications(event, title, body);
    }

    private void handleRideConfirmed(RideNotificationEvent event) {
        String title = "Ride Confirmed";
        String body = "Your request to join the ride has been accepted!";
        persistNotifications(event, title, body);
    }

    private void handleMatchFound(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getSenderUserId(), "A new member");
        String title = "New Crew Member";
        String body = actorName + " has joined your ride";
        persistNotifications(event, title, body);
    }

    private void handleMemberLeft(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getSenderUserId(), "A member");
        String title = "Member Left";
        String body = actorName + " has left the ride";
        persistNotifications(event, title, body);
    }

    private void persistNotifications(RideNotificationEvent event, String title, String body) {
        if (event.getRecipientUserIds() == null || event.getRecipientUserIds().isEmpty()) {
            return;
        }

        List<Notification> notifications = new ArrayList<>();
        for (String recipientId : event.getRecipientUserIds()) {
            Notification notification = Notification.Builder.notification()
                    .withNotificationId(sequenceGenerator.nextId())
                    .withUserId(recipientId)
                    .withTitle(title)
                    .withBody(body)
                    .withType(event.getType().name())
                    .withSenderUserId(event.getSenderUserId())
                    .withRideId(event.getRideId())
                    .withIsRead(false)
                    .build();
            notifications.add(notification);
        }

        notificationService.saveInAppNotifications(notifications);
    }

    private String resolveUserName(String userId, String fallback) {
        try {
            UserDetail user = userDetailService.getUserDetailEntity(userId);
            return user.getName() != null ? user.getName() : fallback;
        } catch (Exception e) {
            log.warn("Could not resolve user name for persisting notification {}: {}", userId, e.getMessage());
            return fallback;
        }
    }
}
