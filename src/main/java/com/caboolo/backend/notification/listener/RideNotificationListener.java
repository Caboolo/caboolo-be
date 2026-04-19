package com.caboolo.backend.notification.listener;

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

import java.util.Map;

/**
 * Listens for {@link RideNotificationEvent}s and sends FCM push notifications.
 * <p>
 * Runs AFTER_COMMIT so notifications are only sent when the transaction succeeds.
 * Runs @Async so it never blocks the calling thread.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RideNotificationListener {

    private final NotificationService notificationService;
    private final UserDetailService userDetailService;

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
            log.error("Failed to process ride notification [type={}, rideId={}]: {}",
                    event.getType(), event.getRideId(), e.getMessage());
        }
    }

    private void handleRideRequestSent(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getActorUserId(), "Someone");

        notificationService.sendToUsers(
                event.getRecipientUserIds(),
                "New Join Request",
                actorName + " wants to join your ride",
                Map.of(
                        "rideId", String.valueOf(event.getRideId()),
                        "requesterId", event.getActorUserId(),
                        "type", "RIDE_REQUEST_SENT"
                )
        );
    }

    private void handleRideConfirmed(RideNotificationEvent event) {
        notificationService.sendToUsers(
                event.getRecipientUserIds(),
                "Ride Confirmed. ",
                "Your request to join the ride has been accepted!",
                Map.of(
                        "rideId", String.valueOf(event.getRideId()),
                        "type", "RIDE_CONFIRMED"
                )
        );
    }

    private void handleMatchFound(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getActorUserId(), "A new member");

        notificationService.sendToUsers(
                event.getRecipientUserIds(),
                "New Crew Member",
                actorName + " has joined your ride",
                Map.of(
                        "rideId", String.valueOf(event.getRideId()),
                        "requesterId", event.getActorUserId(),
                        "type", "MATCH_FOUND"
                )
        );
    }

    private void handleMemberLeft(RideNotificationEvent event) {
        String actorName = resolveUserName(event.getActorUserId(), "A member");

        notificationService.sendToUsers(
                event.getRecipientUserIds(),
                "Member Left",
                actorName + " has left the ride",
                Map.of(
                        "rideId", String.valueOf(event.getRideId()),
                        "userId", event.getActorUserId(),
                        "type", "MEMBER_LEFT"
                )
        );
    }

    private String resolveUserName(String userId, String fallback) {
        try {
            UserDetail user = userDetailService.getUserDetailEntity(userId);
            return user.getName() != null ? user.getName() : fallback;
        } catch (Exception e) {
            log.warn("Could not resolve user name for {}: {}", userId, e.getMessage());
            return fallback;
        }
    }
}
