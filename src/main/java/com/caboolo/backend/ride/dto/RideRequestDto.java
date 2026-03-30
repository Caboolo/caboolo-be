package com.caboolo.backend.ride.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {
    private String userId;
    private String sourceHubId;
    private String destinationHubId;
    private LocalDateTime departureTime;
    private Integer totalSeats;
    private boolean isWomenOnlyRide;
    private BigDecimal poolPrice;
}
