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
    private List<RideParticipantDto> participants;
}
