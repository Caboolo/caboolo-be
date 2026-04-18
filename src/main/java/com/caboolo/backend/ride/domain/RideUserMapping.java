package com.caboolo.backend.ride.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ride_user_mapping")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RideUserMapping extends GenericIdEntity {

    @Column(name = "ride_user_mapping_id", nullable = false)
    private String rideUserMappingId;

    @Column(name = "ride_id", nullable = false)
    private String rideId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideUserMappingStatus status;

    public static interface RideUserMappingIdStep {
        RideIdStep withRideUserMappingId(String rideUserMappingId);
    }

    public static interface RideIdStep {
        UserIdStep withRideId(String rideId);
    }

    public static interface UserIdStep {
        StatusStep withUserId(String userId);
    }

    public static interface StatusStep {
        BuildStep withStatus(RideUserMappingStatus status);
    }

    public static interface BuildStep {
        RideUserMapping build();
    }

    public static class Builder implements RideUserMappingIdStep, RideIdStep, UserIdStep, StatusStep, BuildStep {
        private String rideUserMappingId;
        private String rideId;
        private String userId;
        private RideUserMappingStatus status;

        private Builder() {
        }

        public static RideUserMappingIdStep rideUserMapping() {
            return new Builder();
        }

        @Override
        public RideIdStep withRideUserMappingId(String rideUserMappingId) {
            this.rideUserMappingId = rideUserMappingId;
            return this;
        }

        @Override
        public UserIdStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public StatusStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public BuildStep withStatus(RideUserMappingStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public RideUserMapping build() {
            return new RideUserMapping(
                    this.rideUserMappingId,
                    this.rideId,
                    this.userId,
                    this.status
            );
        }
    }
}
