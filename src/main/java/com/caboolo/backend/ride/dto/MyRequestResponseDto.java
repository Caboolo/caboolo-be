package com.caboolo.backend.ride.dto;

import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyRequestResponseDto {
    private RideUserMappingStatus requestStatus;
    private RideStatus rideStatus;
    private List<PassengerInfoDto> activePassengers;

    public Long getRideId() { return rideId; }
    public String getSourceHubName() { return sourceHubName; }
    public String getDestinationHubName() { return destinationHubName; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getAvailableSeats() { return availableSeats; }
    public Integer getPoolPrice() { return poolPrice; }
    public RideUserMappingStatus getRequestStatus() { return requestStatus; }
    public RideStatus getRideStatus() { return rideStatus; }
    public List<PassengerInfoDto> getActivePassengers() { return activePassengers; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long rideId;
        private String sourceHubName;
        private String destinationHubName;
        private LocalDateTime departureTime;
        private Integer totalSeats;
        private Integer availableSeats;
        private Integer poolPrice;
        private RideUserMappingStatus requestStatus;
        private RideStatus rideStatus;
        private List<PassengerInfoDto> activePassengers;

        public Builder rideId(Long rideId) { this.rideId = rideId; return this; }
        public Builder sourceHubName(String sourceHubName) { this.sourceHubName = sourceHubName; return this; }
        public Builder destinationHubName(String destinationHubName) { this.destinationHubName = destinationHubName; return this; }
        public Builder departureTime(LocalDateTime departureTime) { this.departureTime = departureTime; return this; }
        public Builder totalSeats(Integer totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder availableSeats(Integer availableSeats) { this.availableSeats = availableSeats; return this; }
        public Builder poolPrice(Integer poolPrice) { this.poolPrice = poolPrice; return this; }
        public Builder requestStatus(RideUserMappingStatus requestStatus) { this.requestStatus = requestStatus; return this; }
        public Builder rideStatus(RideStatus rideStatus) { this.rideStatus = rideStatus; return this; }
        public Builder activePassengers(List<PassengerInfoDto> activePassengers) { this.activePassengers = activePassengers; return this; }
        public MyRequestResponseDto build() {
            return new MyRequestResponseDto(rideId, sourceHubName, destinationHubName, departureTime, totalSeats, availableSeats, poolPrice, requestStatus, rideStatus, activePassengers);
        }
    }
}
