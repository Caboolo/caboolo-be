package com.caboolo.backend.ride.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.dto.MyRideResponseDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.service.RideService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ride")
public class RideController extends BaseController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping("/create")
    public RestEntity<Long> createRide(@RequestBody RideRequestDto request) {
        Long rideId = rideService.createRide(request);
        return successResponse(rideId, "Ride created successfully");
    }

    @GetMapping("/my-rides")
    public RestEntity<List<MyRideResponseDto>> getMyRides(@RequestParam String userId) {
        return successResponse(rideService.getMyRides(userId), "My rides retrieved successfully");
    }
}
