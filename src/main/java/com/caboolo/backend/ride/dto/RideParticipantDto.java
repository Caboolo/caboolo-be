package com.caboolo.backend.ride.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideParticipantDto {
    private String userId;
    private String name;
    private Double avgRating;
    private String imageUrl;
}
