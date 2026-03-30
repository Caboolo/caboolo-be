package com.caboolo.backend.ride.dto;

import lombok.*;
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
    private List<PassengerInfoDto> participants;

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
        BuildStep withParticipants(List<PassengerInfoDto> participants);
    }

    public static interface BuildStep {
        MyRideResponseDto build();
    }


    public static class Builder implements RideIdStep, DepartureTimeStep, SourceHubNameStep, DestinationHubNameStep, ParticipantsStep, BuildStep {
        private Long rideId;
        private LocalDateTime departureTime;
        private String sourceHubName;
        private String destinationHubName;
        private List<PassengerInfoDto> participants;

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
        public BuildStep withParticipants(List<PassengerInfoDto> participants) {
            this.participants = participants;
            return this;
        }

        @Override
        public MyRideResponseDto build() {
            return new MyRideResponseDto(
                    this.rideId,
                    this.departureTime,
                    this.sourceHubName,
                    this.destinationHubName,
                    this.participants
            );
        }
    }
}
