package com.caboolo.backend.ride.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.MyRideResponseDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.service.RideService;
import com.caboolo.backend.ride.service.RideUserMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ride")
public class RideController extends BaseController {

    private final RideService rideService;
    private final RideUserMappingService rideUserMappingService;

    public RideController(RideService rideService, RideUserMappingService rideUserMappingService) {
        this.rideService = rideService;
        this.rideUserMappingService = rideUserMappingService;
    }

    @PostMapping("/create")
    public RestEntity<Long> createRide(@RequestBody RideRequestDto request) {
        log.info("Creating new ride for user: {}", request.getUserId());
        Long rideId = rideService.createRide(request);
        log.info("Ride created successfully with id: {}", rideId);
        return successResponse(rideId, "Ride created successfully");
    }

    @GetMapping("/my-requests")
    public RestEntity<List<MyRequestResponseDto>> getMyRequests(@RequestParam String userId) {
        return successResponse(rideService.getMyRequests(userId));
    }

    @PutMapping("/request/{rideId}/withdraw")
    public RestEntity<Void> withdrawRequest(@PathVariable Long rideId, @RequestParam String userId) {
        rideUserMappingService.withdrawRequest(rideId, userId);
        return successResponse("Request withdrawn successfully");
    }

    @PatchMapping("/{rideId}/pool-price")
    public RestEntity<Void> updatePoolPrice(@PathVariable Long rideId, @RequestParam String userId, @RequestParam BigDecimal poolPrice) {
        rideService.updatePoolPrice(rideId, userId, poolPrice);
        return successResponse("Pool price updated successfully");
    }

    @GetMapping("/my-rides")
    public RestEntity<List<MyRideResponseDto>> getMyRides(@RequestParam String userId) {
        return successResponse(rideService.getMyRides(userId), "My rides retrieved successfully");
    }

    @GetMapping("/listing")
    public RestEntity<List<MyRideResponseDto>> getAvailableRides(
            @RequestParam String userId,
            @RequestParam@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam(required = false, defaultValue = "15") Integer timeWindow,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String airportHubId,
            @RequestParam Boolean isFromAirport) {
        
        return successResponse(rideService.getAvailableRides(userId, time, timeWindow, latitude, longitude, airportHubId, isFromAirport), "Available rides retrieved successfully");
    }
}
