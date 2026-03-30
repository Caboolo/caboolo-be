package com.caboolo.backend.flightverification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightVerificationRequestDto {

    private String flightNumber;
    private LocalDate flightDate;
}
