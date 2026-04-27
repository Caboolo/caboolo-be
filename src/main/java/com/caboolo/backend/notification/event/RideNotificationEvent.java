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
    private final String senderUserId;

    /** Users who should receive the notification */
    private final List<String> recipientUserIds;

    /** The notification title */
    private final String title;

    /** The notification message body */
    private final String body;

    private RideNotificationEvent(RideNotificationType type, String rideId,
                                  String senderUserId, List<String> recipientUserIds,
                                  String title, String body) {
        this.type = type;
        this.rideId = rideId;
        this.senderUserId = senderUserId;
        this.recipientUserIds = recipientUserIds;
        this.title = title;
        this.body = body;
    }

    public static RideNotificationEvent of(RideNotificationType type, String rideId,
                                            String senderUserId, List<String> recipientUserIds,
                                            String title, String body) {
        return new RideNotificationEvent(type, rideId, senderUserId, recipientUserIds, title, body);
    }
}
