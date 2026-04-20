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
    private String rideUserRequestMappingId;

    @Column(name = "ride_id", nullable = false)
    private String rideId;

    @Column(name = "requestor_id", nullable = false)
    private String requestorId;

    @Column(name = "approver_id", nullable = false)
    private String approverId;

    @Column(name = "ride_user_mapping_id")
    private String rideUserMappingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideUserRequestStatus status;

    @Column(name = "comment")
    private String comment;

    public static interface RideUserRequestMappingIdStep {
        RideIdStep withRideUserRequestMappingId(String rideUserRequestMappingId);
    }

    public static interface RideIdStep {
        RequestorIdStep withRideId(String rideId);
    }

    public static interface RequestorIdStep {
        ApproverIdStep withRequestorId(String requestorId);
    }

    public static interface ApproverIdStep {
        RideUserMappingIdStep withApproverId(String approverId);
    }

    public static interface RideUserMappingIdStep {
        StatusStep withRideUserMappingId(String rideUserMappingId);
    }

    public static interface StatusStep {
        BuildStep withStatus(RideUserRequestStatus status);
    }

    public static interface BuildStep {
        BuildStep withComment(String comment);
        RideUserRequestMapping build();
    }

    public static class Builder implements RideUserRequestMappingIdStep, RideIdStep, RequestorIdStep,
            ApproverIdStep, RideUserMappingIdStep, StatusStep, BuildStep {

        private String rideUserRequestMappingId;
        private String rideId;
        private String requestorId;
        private String approverId;
        private String rideUserMappingId;
        private RideUserRequestStatus status;
        private String comment;

        private Builder() {
        }

        public static RideUserRequestMappingIdStep rideUserRequestMapping() {
            return new Builder();
        }

        @Override
        public RideIdStep withRideUserRequestMappingId(String rideUserRequestMappingId) {
            this.rideUserRequestMappingId = rideUserRequestMappingId;
            return this;
        }

        @Override
        public RequestorIdStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public ApproverIdStep withRequestorId(String requestorId) {
            this.requestorId = requestorId;
            return this;
        }

        @Override
        public RideUserMappingIdStep withApproverId(String approverId) {
            this.approverId = approverId;
            return this;
        }

        @Override
        public StatusStep withRideUserMappingId(String rideUserMappingId) {
            this.rideUserMappingId = rideUserMappingId;
            return this;
        }

        @Override
        public BuildStep withStatus(RideUserRequestStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public BuildStep withComment(String comment) {
            this.comment = comment;
            return this;
        }

        @Override
        public RideUserRequestMapping build() {
            return new RideUserRequestMapping(
                    this.rideUserRequestMappingId,
                    this.rideId,
                    this.requestorId,
                    this.approverId,
                    this.rideUserMappingId,
                    this.status,
                    this.comment
            );
        }
    }
}
