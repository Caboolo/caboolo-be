package com.caboolo.backend.notification.event;

/**
 * Types of ride-related notifications.
 * <p>
 * Each type carries a {@code screen} string that the FE uses to navigate
 * the user to the correct screen when a notification is tapped.
 */
public enum RideNotificationType {
    /** A user requested to join a ride → sent to crew members */
    RIDE_REQUEST_SENT(true, "RideRequests"),

    /** A request was accepted (≥50% threshold) → sent to the requester */
    RIDE_CONFIRMED(true, "RideDetail"),

    /** A new member joined → sent to existing crew */
    MATCH_FOUND(true, "RideDetail"),

    /** An active member left the ride → sent to remaining crew */
    MEMBER_LEFT(true, "RideDetail");

    private final boolean shouldPersist;

    /** FE screen/route name to navigate to when this notification is tapped */
    private final String screen;

    RideNotificationType(boolean shouldPersist, String screen) {
        this.shouldPersist = shouldPersist;
        this.screen = screen;
    }

    public boolean shouldPersist() {
        return shouldPersist;
    }

    public String getScreen() {
        return screen;
    }
}
