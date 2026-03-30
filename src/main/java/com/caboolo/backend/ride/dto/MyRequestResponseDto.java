package com.caboolo.backend.ride.dto;

import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRequestResponseDto {
    private RideUserMappingStatus requestStatus;
    private String sourceHubName;
    private String destinationHubName;
    private LocalDateTime departureTime;
    private List<PassengerInfoDto> activePassengers;
    private Integer availableSeats;
    private BigDecimal poolPrice;

    public static interface RequestStatusStep {
        SourceHubNameStep withRequestStatus(RideUserMappingStatus requestStatus);
    }

    public static interface SourceHubNameStep {
        DestinationHubNameStep withSourceHubName(String sourceHubName);
    }

    public static interface DestinationHubNameStep {
        DepartureTimeStep withDestinationHubName(String destinationHubName);
    }

    public static interface DepartureTimeStep {
        ActivePassengersStep withDepartureTime(LocalDateTime departureTime);
    }

    public static interface ActivePassengersStep {
        AvailableSeatsStep withActivePassengers(List<PassengerInfoDto> activePassengers);
    }
    
    public static interface AvailableSeatsStep {
        PoolPriceStep withAvailableSeats(Integer availableSeats);
    }
    
    public static interface PoolPriceStep {
        BuildStep withPoolPrice(BigDecimal poolPrice);
    }

    public static interface BuildStep {
        MyRequestResponseDto build();
    }


    public static class Builder implements RequestStatusStep, SourceHubNameStep, DestinationHubNameStep, DepartureTimeStep, ActivePassengersStep, AvailableSeatsStep, PoolPriceStep, BuildStep {
        private RideUserMappingStatus requestStatus;
        private String sourceHubName;
        private String destinationHubName;
        private LocalDateTime departureTime;
        private List<PassengerInfoDto> activePassengers;
        private Integer availableSeats;
        private BigDecimal poolPrice;

        private Builder() {
        }

        public static RequestStatusStep myRequestResponseDto() {
            return new Builder();
        }

        @Override
        public SourceHubNameStep withRequestStatus(RideUserMappingStatus requestStatus) {
            this.requestStatus = requestStatus;
            return this;
        }

        @Override
        public DestinationHubNameStep withSourceHubName(String sourceHubName) {
            this.sourceHubName = sourceHubName;
            return this;
        }

        @Override
        public DepartureTimeStep withDestinationHubName(String destinationHubName) {
            this.destinationHubName = destinationHubName;
            return this;
        }

        @Override
        public ActivePassengersStep withDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        @Override
        public AvailableSeatsStep withActivePassengers(List<PassengerInfoDto> activePassengers) {
            this.activePassengers = activePassengers;
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
        public MyRequestResponseDto build() {
            return new MyRequestResponseDto(
                    this.requestStatus,
                    this.sourceHubName,
                    this.destinationHubName,
                    this.departureTime,
                    this.activePassengers,
                    this.availableSeats,
                    this.poolPrice
            );
        }
    }
}
