package com.caboolo.backend.notification.event;

/**
 * Types of ride-related notifications.
 */
public enum RideNotificationType {
    /** A user requested to join a ride → sent to crew members */
    RIDE_REQUEST_SENT(true),

    /** A request was accepted (≥50% threshold) → sent to the requester */
    RIDE_CONFIRMED(true),

    /** A new member joined → sent to existing crew */
    MATCH_FOUND(true),

    /** An active member left the ride → sent to remaining crew */
    MEMBER_LEFT(true);

    private final boolean shouldPersist;

    RideNotificationType(boolean shouldPersist) {
        this.shouldPersist = shouldPersist;
    }

    public boolean shouldPersist() {
        return shouldPersist;
    }
}
