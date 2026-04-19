package com.caboolo.backend.notification.event;

import lombok.Getter;

import java.util.List;

/**
 * Event published by ride services when a notification-worthy action occurs.
 * <p>
 * Carries just enough data for the listener to resolve user details
 * and send push notifications after the transaction commits.
 */
@Getter
public class RideNotificationEvent {

    private final RideNotificationType type;
    private final String rideId;

    /** The user who triggered the event (requester / leaver) */
    private final String actorUserId;

    /** Users who should receive the notification */
    private final List<String> recipientUserIds;

    private RideNotificationEvent(RideNotificationType type, String rideId,
                                  String actorUserId, List<String> recipientUserIds) {
        this.type = type;
        this.rideId = rideId;
        this.actorUserId = actorUserId;
        this.recipientUserIds = recipientUserIds;
    }

    public static RideNotificationEvent of(RideNotificationType type, String rideId,
                                            String actorUserId, List<String> recipientUserIds) {
        return new RideNotificationEvent(type, rideId, actorUserId, recipientUserIds);
    }
}
