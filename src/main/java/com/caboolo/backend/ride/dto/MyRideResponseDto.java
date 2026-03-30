package com.caboolo.backend.ride.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyRideResponseDto {
    private Long rideId;
    private LocalDateTime departureTime;
    private String sourceHubName;
    private String destinationHubName;
    private List<RiderInfoDto> participants;
    private Integer availableSeats;
    private BigDecimal poolPrice;

    public static interface RideIdStep {
        DepartureTimeStep withRideId(Long rideId);
    }

    public static interface DepartureTimeStep {
        SourceHubNameStep withDepartureTime(LocalDateTime departureTime);
    }

    public static interface SourceHubNameStep {
        DestinationHubNameStep withSourceHubName(String sourceHubName);
    }

    public static interface DestinationHubNameStep {
        ParticipantsStep withDestinationHubName(String destinationHubName);
    }

    public static interface ParticipantsStep {
        AvailableSeatsStep withParticipants(List<RiderInfoDto> participants);
    }

    public static interface AvailableSeatsStep {
        PoolPriceStep withAvailableSeats(Integer availableSeats);
    }

    public static interface PoolPriceStep {
        BuildStep withPoolPrice(BigDecimal poolPrice);
    }

    public static interface BuildStep {
        MyRideResponseDto build();
    }


    public static class Builder implements RideIdStep, DepartureTimeStep, SourceHubNameStep, DestinationHubNameStep, ParticipantsStep, AvailableSeatsStep, PoolPriceStep, BuildStep {
        private Long rideId;
        private LocalDateTime departureTime;
        private String sourceHubName;
        private String destinationHubName;
        private List<RiderInfoDto> participants;
        private Integer availableSeats;
        private BigDecimal poolPrice;

        private Builder() {
        }

        public static RideIdStep myRideResponseDto() {
            return new Builder();
        }

        @Override
        public DepartureTimeStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public SourceHubNameStep withDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        @Override
        public DestinationHubNameStep withSourceHubName(String sourceHubName) {
            this.sourceHubName = sourceHubName;
            return this;
        }

        @Override
        public ParticipantsStep withDestinationHubName(String destinationHubName) {
            this.destinationHubName = destinationHubName;
            return this;
        }

        @Override
        public AvailableSeatsStep withParticipants(List<RiderInfoDto> participants) {
            this.participants = participants;
            return this;
        }

        @Override
        public PoolPriceStep withAvailableSeats(Integer availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        @Override
        public BuildStep withPoolPrice(BigDecimal poolPrice) {
            this.poolPrice = poolPrice;
            return this;
        }

        @Override
        public MyRideResponseDto build() {
            return new MyRideResponseDto(
                    this.rideId,
                    this.departureTime,
                    this.sourceHubName,
                    this.destinationHubName,
                    this.participants,
                    this.availableSeats,
                    this.poolPrice
            );
        }
    }
}
