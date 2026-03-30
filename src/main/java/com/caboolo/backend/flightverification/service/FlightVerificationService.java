package com.caboolo.backend.flightverification.service;

import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;

public interface FlightVerificationService {

    FlightVerificationResponseDto verifyFlight(String userId, FlightVerificationRequestDto request);
}
