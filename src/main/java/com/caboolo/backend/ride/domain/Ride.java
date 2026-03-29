package com.caboolo.backend.ride.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.ride.enums.RideStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Ride extends GenericIdEntity {

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "source_hub_id", nullable = false)
    private String sourceHubId;

    @Column(name = "destination_hub_id", nullable = false)
    private String destinationHubId;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideStatus status;

    @Column(name = "is_women_only_ride", nullable = false)
    private boolean isWomenOnlyRide;

    public static interface RideIdStep {
        SourceHubIdStep withRideId(Long rideId);
    }

    public static interface SourceHubIdStep {
        DestinationHubIdStep withSourceHubId(String sourceHubId);
    }

    public static interface DestinationHubIdStep {
        DepartureTimeStep withDestinationHubId(String destinationHubId);
    }

    public static interface DepartureTimeStep {
        TotalSeatsStep withDepartureTime(LocalDateTime departureTime);
    }

    public static interface TotalSeatsStep {
        StatusStep withTotalSeats(Integer totalSeats);
    }

    public static interface StatusStep {
        IsWomenOnlyRideStep withStatus(RideStatus status);
    }

    public static interface IsWomenOnlyRideStep {
        BuildStep withIsWomenOnlyRide(boolean isWomenOnlyRide);
    }

    public static interface BuildStep {
        Ride build();
    }


    public static class Builder implements RideIdStep, SourceHubIdStep, DestinationHubIdStep, DepartureTimeStep, TotalSeatsStep, StatusStep, IsWomenOnlyRideStep, BuildStep {
        private Long rideId;
        private String sourceHubId;
        private String destinationHubId;
        private LocalDateTime departureTime;
        private Integer totalSeats;
        private RideStatus status;
        private boolean isWomenOnlyRide;

        private Builder() {
        }

        public static RideIdStep ride() {
            return new Builder();
        }

        @Override
        public SourceHubIdStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public DestinationHubIdStep withSourceHubId(String sourceHubId) {
            this.sourceHubId = sourceHubId;
            return this;
        }

        @Override
        public DepartureTimeStep withDestinationHubId(String destinationHubId) {
            this.destinationHubId = destinationHubId;
            return this;
        }

        @Override
        public TotalSeatsStep withDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        @Override
        public StatusStep withTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        @Override
        public IsWomenOnlyRideStep withStatus(RideStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public BuildStep withIsWomenOnlyRide(boolean isWomenOnlyRide) {
            this.isWomenOnlyRide = isWomenOnlyRide;
            return this;
        }

        @Override
        public Ride build() {
            return new Ride(
                    this.rideId,
                    this.sourceHubId,
                    this.destinationHubId,
                    this.departureTime,
                    this.totalSeats,
                    this.status,
                    this.isWomenOnlyRide
            );
        }
    }
}
