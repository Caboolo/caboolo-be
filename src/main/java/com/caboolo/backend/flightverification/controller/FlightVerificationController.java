package com.caboolo.backend.flightverification.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;
import com.caboolo.backend.flightverification.service.FlightVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/flight-verification")
public class FlightVerificationController extends BaseController {

    private final FlightVerificationService flightVerificationService;

    public FlightVerificationController(FlightVerificationService flightVerificationService) {
        this.flightVerificationService = flightVerificationService;
    }

    /**
     * Verifies a flight for the given user.
     *
     * POST /api/v1/flight-verification/verify?userId={userId}
     */
    @PostMapping("/verify")
    public RestEntity<FlightVerificationResponseDto> verifyFlight(
            @RequestParam String userId,
            @Valid @RequestBody FlightVerificationRequestDto request) {
        log.info("Flight verification request received for userId={}, flightNumber={}", userId, request.getFlightNumber());
        FlightVerificationResponseDto response = flightVerificationService.verifyFlight(userId, request);
        log.info("Flight verification completed for userId={}, flightNumber={}, status={}",
                userId, request.getFlightNumber(), response.getStatus());
        return successResponse(response);
    }
}
