package com.caboolo.backend.ride.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinRideRequestDto {
    private String requesterId;
    private String comment;
}
