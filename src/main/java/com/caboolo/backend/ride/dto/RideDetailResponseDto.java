package com.caboolo.backend.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideDetailResponseDto {
    private String rideId;
    private LocalDateTime departureTime;
    private String sourceHubName;
    private Double sourceHubLatitude;
    private Double sourceHubLongitude;
    private String destinationHubName;
    private Double destinationHubLatitude;
    private Double destinationHubLongitude;
    private BigDecimal poolPrice;
    private Integer totalSeats;
    private Integer availableSeats;
    private List<RideParticipantDto> participants;
}
