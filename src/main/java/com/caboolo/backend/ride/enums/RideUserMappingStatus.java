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
    public static final Set<RideUserMappingStatus> VISIBLE_STATUSES = EnumSet.of(CREATED, ACCEPTED, PENDING); // visible in FE my-ride detali page
    public static final Set<RideUserMappingStatus> INACTIVE_STATUSES = EnumSet.of(REJECTED, WITHDRAWN, LEFT);
    public static final Set<RideUserMappingStatus> ALL_STATUSES = EnumSet.allOf(RideUserMappingStatus.class);
}
