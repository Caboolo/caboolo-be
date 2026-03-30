package com.caboolo.backend.ride.enums;

import java.util.EnumSet;
import java.util.Set;

public enum RideUserMappingStatus {
    CREATED,
    PENDING,
    ACCEPTED,
    LEFT,
    REJECTED,
    WITHDRAWN;

    public static final Set<RideUserMappingStatus> ACTIVE_STATUSES = EnumSet.of(CREATED, ACCEPTED);
}
