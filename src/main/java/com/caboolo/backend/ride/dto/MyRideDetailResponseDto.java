package com.caboolo.backend.ride.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRideDetailResponseDto {
    private Long rideId;
    private LocalDateTime departureTime;
    private String sourceHubName;
    private String destinationHubName;
    private BigDecimal poolPrice;
    private Integer totalSeats;
    private Integer availableSeats;
    private List<CrewMemberDto> crewMembers;
    private List<PendingRequestDto> pendingRequests;

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
        PoolPriceStep withDestinationHubName(String destinationHubName);
    }

    public static interface PoolPriceStep {
        TotalSeatsStep withPoolPrice(BigDecimal poolPrice);
    }

    public static interface TotalSeatsStep {
        AvailableSeatsStep withTotalSeats(Integer totalSeats);
    }

    public static interface AvailableSeatsStep {
        CrewMembersStep withAvailableSeats(Integer availableSeats);
    }

    public static interface CrewMembersStep {
        PendingRequestsStep withCrewMembers(List<CrewMemberDto> crewMembers);
    }

    public static interface PendingRequestsStep {
        BuildStep withPendingRequests(List<PendingRequestDto> pendingRequests);
    }

    public static interface BuildStep {
        MyRideDetailResponseDto build();
    }

    public static class Builder implements RideIdStep, DepartureTimeStep, SourceHubNameStep, DestinationHubNameStep, PoolPriceStep, TotalSeatsStep, AvailableSeatsStep, CrewMembersStep, PendingRequestsStep, BuildStep {
        private Long rideId;
        private LocalDateTime departureTime;
        private String sourceHubName;
        private String destinationHubName;
        private BigDecimal poolPrice;
        private Integer totalSeats;
        private Integer availableSeats;
        private List<CrewMemberDto> crewMembers;
        private List<PendingRequestDto> pendingRequests;

        private Builder() {
        }

        public static RideIdStep myRideDetailResponseDto() {
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
        public PoolPriceStep withDestinationHubName(String destinationHubName) {
            this.destinationHubName = destinationHubName;
            return this;
        }

        @Override
        public TotalSeatsStep withPoolPrice(BigDecimal poolPrice) {
            this.poolPrice = poolPrice;
            return this;
        }

        @Override
        public AvailableSeatsStep withTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        @Override
        public CrewMembersStep withAvailableSeats(Integer availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        @Override
        public PendingRequestsStep withCrewMembers(List<CrewMemberDto> crewMembers) {
            this.crewMembers = crewMembers;
            return this;
        }

        @Override
        public BuildStep withPendingRequests(List<PendingRequestDto> pendingRequests) {
            this.pendingRequests = pendingRequests;
            return this;
        }

        @Override
        public MyRideDetailResponseDto build() {
            return new MyRideDetailResponseDto(
                    this.rideId,
                    this.departureTime,
                    this.sourceHubName,
                    this.destinationHubName,
                    this.poolPrice,
                    this.totalSeats,
                    this.availableSeats,
                    this.crewMembers,
                    this.pendingRequests
            );
        }
    }
}
