package com.caboolo.backend.flightverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightVerificationRequestDto {

    @NotBlank(message = "flightNumber cannot be null or blank")
    private String flightNumber;

    @NotNull(message = "flightDate cannot be null")
    private LocalDate flightDate;
}
