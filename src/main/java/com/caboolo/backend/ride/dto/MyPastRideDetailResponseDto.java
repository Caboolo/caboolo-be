package com.caboolo.backend.ride.dto;

import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPastRideDetailResponseDto {
    private String rideId;
    private RideUserMappingStatus userStatus;
    private LocalDateTime departureTime;
    private String sourceHubName;
    private Double sourceHubLatitude;
    private Double sourceHubLongitude;
    private String destinationHubName;
    private Double destinationHubLatitude;
    private Double destinationHubLongitude;
    private BigDecimal poolPrice;
    private Integer totalSeats;
    private List<RideParticipantDto> participants;

    public static interface RideIdStep {
        UserStatusStep withRideId(String rideId);
    }

    public static interface UserStatusStep {
        DepartureTimeStep withUserStatus(RideUserMappingStatus userStatus);
    }

    public static interface DepartureTimeStep {
        SourceHubNameStep withDepartureTime(LocalDateTime departureTime);
    }

    public static interface SourceHubNameStep {
        SourceHubLatitudeStep withSourceHubName(String sourceHubName);
    }

    public static interface SourceHubLatitudeStep {
        SourceHubLongitudeStep withSourceHubLatitude(Double sourceHubLatitude);
    }

    public static interface SourceHubLongitudeStep {
        DestinationHubNameStep withSourceHubLongitude(Double sourceHubLongitude);
    }

    public static interface DestinationHubNameStep {
        DestinationHubLatitudeStep withDestinationHubName(String destinationHubName);
    }

    public static interface DestinationHubLatitudeStep {
        DestinationHubLongitudeStep withDestinationHubLatitude(Double destinationHubLatitude);
    }

    public static interface DestinationHubLongitudeStep {
        PoolPriceStep withDestinationHubLongitude(Double destinationHubLongitude);
    }

    public static interface PoolPriceStep {
        TotalSeatsStep withPoolPrice(BigDecimal poolPrice);
    }

    public static interface TotalSeatsStep {
        ParticipantsStep withTotalSeats(Integer totalSeats);
    }

    public static interface ParticipantsStep {
        BuildStep withParticipants(List<RideParticipantDto> participants);
    }

    public static interface BuildStep {
        MyPastRideDetailResponseDto build();
    }

    public static class Builder implements RideIdStep, UserStatusStep, DepartureTimeStep, SourceHubNameStep,
            SourceHubLatitudeStep, SourceHubLongitudeStep, DestinationHubNameStep, DestinationHubLatitudeStep,
            DestinationHubLongitudeStep, PoolPriceStep, TotalSeatsStep, ParticipantsStep, BuildStep {

        private String rideId;
        private RideUserMappingStatus userStatus;
        private LocalDateTime departureTime;
        private String sourceHubName;
        private Double sourceHubLatitude;
        private Double sourceHubLongitude;
        private String destinationHubName;
        private Double destinationHubLatitude;
        private Double destinationHubLongitude;
        private BigDecimal poolPrice;
        private Integer totalSeats;
        private List<RideParticipantDto> participants;

        private Builder() {
        }

        public static RideIdStep myPastRideDetailResponseDto() {
            return new Builder();
        }

        @Override
        public UserStatusStep withRideId(String rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public DepartureTimeStep withUserStatus(RideUserMappingStatus userStatus) {
            this.userStatus = userStatus;
            return this;
        }

        @Override
        public SourceHubNameStep withDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        @Override
        public SourceHubLatitudeStep withSourceHubName(String sourceHubName) {
            this.sourceHubName = sourceHubName;
            return this;
        }

        @Override
        public SourceHubLongitudeStep withSourceHubLatitude(Double sourceHubLatitude) {
            this.sourceHubLatitude = sourceHubLatitude;
            return this;
        }

        @Override
        public DestinationHubNameStep withSourceHubLongitude(Double sourceHubLongitude) {
            this.sourceHubLongitude = sourceHubLongitude;
            return this;
        }

        @Override
        public DestinationHubLatitudeStep withDestinationHubName(String destinationHubName) {
            this.destinationHubName = destinationHubName;
            return this;
        }

        @Override
        public DestinationHubLongitudeStep withDestinationHubLatitude(Double destinationHubLatitude) {
            this.destinationHubLatitude = destinationHubLatitude;
            return this;
        }

        @Override
        public PoolPriceStep withDestinationHubLongitude(Double destinationHubLongitude) {
            this.destinationHubLongitude = destinationHubLongitude;
            return this;
        }

        @Override
        public TotalSeatsStep withPoolPrice(BigDecimal poolPrice) {
            this.poolPrice = poolPrice;
            return this;
        }

        @Override
        public ParticipantsStep withTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        @Override
        public BuildStep withParticipants(List<RideParticipantDto> participants) {
            this.participants = participants;
            return this;
        }

        @Override
        public MyPastRideDetailResponseDto build() {
            return new MyPastRideDetailResponseDto(
                    this.rideId,
                    this.userStatus,
                    this.departureTime,
                    this.sourceHubName,
                    this.sourceHubLatitude,
                    this.sourceHubLongitude,
                    this.destinationHubName,
                    this.destinationHubLatitude,
                    this.destinationHubLongitude,
                    this.poolPrice,
                    this.totalSeats,
                    this.participants
            );
        }
    }
}
