package com.caboolo.backend.ride.dto;

import lombok.*;
import java.time.LocalDateTime;

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
    private Integer poolPrice;
}
