package com.caboolo.backend.ride.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.ride.enums.RideUserRequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ride_user_request_mapping"
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RideUserRequestMapping extends GenericIdEntity {

    @Column(name = "ride_user_request_mapping_id", nullable = false)
    private Long rideUserRequestMappingId;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "source_user_id", nullable = false)
    private String sourceUserId;

    @Column(name = "destination_user_id", nullable = false)
    private String destinationUserId;

    @Column(name = "ride_user_mapping_id")
    private Long rideUserMappingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideUserRequestStatus status;

    public static interface RideUserRequestMappingIdStep {
        RideIdStep withRideUserRequestMappingId(Long rideUserRequestMappingId);
    }

    public static interface RideIdStep {
        SourceUserIdStep withRideId(Long rideId);
    }

    public static interface SourceUserIdStep {
        DestinationUserIdStep withSourceUserId(String sourceUserId);
    }

    public static interface DestinationUserIdStep {
        RideUserMappingIdStep withDestinationUserId(String destinationUserId);
    }

    public static interface RideUserMappingIdStep {
        StatusStep withRideUserMappingId(Long rideUserMappingId);
    }

    public static interface StatusStep {
        BuildStep withStatus(RideUserRequestStatus status);
    }

    public static interface BuildStep {
        RideUserRequestMapping build();
    }

    public static class Builder implements RideUserRequestMappingIdStep, RideIdStep, SourceUserIdStep,
            DestinationUserIdStep, RideUserMappingIdStep, StatusStep, BuildStep {

        private Long rideUserRequestMappingId;
        private Long rideId;
        private String sourceUserId;
        private String destinationUserId;
        private Long rideUserMappingId;
        private RideUserRequestStatus status;

        private Builder() {
        }

        public static RideUserRequestMappingIdStep rideUserRequestMapping() {
            return new Builder();
        }

        @Override
        public RideIdStep withRideUserRequestMappingId(Long rideUserRequestMappingId) {
            this.rideUserRequestMappingId = rideUserRequestMappingId;
            return this;
        }

        @Override
        public SourceUserIdStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public DestinationUserIdStep withSourceUserId(String sourceUserId) {
            this.sourceUserId = sourceUserId;
            return this;
        }

        @Override
        public RideUserMappingIdStep withDestinationUserId(String destinationUserId) {
            this.destinationUserId = destinationUserId;
            return this;
        }

        @Override
        public StatusStep withRideUserMappingId(Long rideUserMappingId) {
            this.rideUserMappingId = rideUserMappingId;
            return this;
        }

        @Override
        public BuildStep withStatus(RideUserRequestStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public RideUserRequestMapping build() {
            return new RideUserRequestMapping(
                    this.rideUserRequestMappingId,
                    this.rideId,
                    this.sourceUserId,
                    this.destinationUserId,
                    this.rideUserMappingId,
                    this.status
            );
        }
    }
}
