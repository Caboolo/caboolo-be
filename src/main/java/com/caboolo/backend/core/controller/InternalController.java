package com.caboolo.backend.core.controller;

import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/internal/jobs")
@RequiredArgsConstructor
public class InternalController extends BaseController {

    private final RideService rideService;

    @PostMapping("/mark-rides-completed")
    public RestEntity<String> markRidesAsCompleted() {
        log.info("Received internal request to mark rides as COMPLETED");
        int count = rideService.markRidesAsCompleted();
        return successResponse("Successfully processed. Rides marked as COMPLETED: " + count);
    }

    @PostMapping("/mark-rides-ongoing")
    public RestEntity<String> markRidesAsOngoing() {
        log.info("Received internal request to mark rides as ONGOING");
        int count = rideService.markRidesAsOngoing();
        return successResponse("Successfully processed. Rides marked as ONGOING: " + count);
    }
}
