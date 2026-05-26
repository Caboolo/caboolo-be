package com.caboolo.backend.flightverification.service;

import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;

import java.util.Set;

public interface FlightVerificationService {

    FlightVerificationResponseDto verifyFlight(String userId, FlightVerificationRequestDto request);

    Set<String> getActiveVerifiedUserIds(Set<String> userIds);
}
