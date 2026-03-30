package com.caboolo.backend.flightverification.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;
import com.caboolo.backend.flightverification.service.FlightVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody FlightVerificationRequestDto request) {
        FlightVerificationResponseDto response = flightVerificationService.verifyFlight(userId, request);
        return successResponse(response);
    }
}
