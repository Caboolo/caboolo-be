package com.caboolo.backend.ride.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {
    @NotBlank(message = "userId cannot be null or blank")
    private String userId;

    @NotNull(message = "sourceHubId cannot be null")
    private Long sourceHubId;

    @NotNull(message = "destinationHubId cannot be null")
    private Long destinationHubId;

    @NotNull(message = "departureTime cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "totalSeats cannot be null")
    @Min(value = 1, message = "totalSeats must be at least 1")
    private Integer totalSeats;

    private boolean isWomenOnlyRide;

    private BigDecimal poolPrice;
}
