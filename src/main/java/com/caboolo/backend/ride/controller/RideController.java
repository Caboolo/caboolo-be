package com.caboolo.backend.ride.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.MyRequestDetailResponseDto;
import com.caboolo.backend.ride.dto.MyRideResponseDto;
import com.caboolo.backend.ride.dto.MyRideDetailResponseDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.service.RideService;
import com.caboolo.backend.ride.service.RideUserMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;

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
    public RestEntity<String> createRide(@RequestBody RideRequestDto request) {
        log.info("Creating new ride for user: {}", request.getUserId());
        String rideId = rideService.createRide(request);
        log.info("Ride created successfully with id: {}", rideId);
        return successResponse(rideId, "Ride created successfully");
    }

    @GetMapping("/my-requests")
    public RestEntity<List<MyRequestResponseDto>> getMyRequests(@RequestParam String userId) {
        return successResponse(rideService.getMyRequests(userId));
    }

    @GetMapping("/my-requests/{rideId}")
    public RestEntity<MyRequestDetailResponseDto> getMyRequestDetail(@PathVariable String rideId, @RequestParam String userId) {
        log.info("Fetching request detail for rideId: {}, userId: {}", rideId, userId);
        return successResponse(rideService.getMyRequestDetail(rideId, userId), "Request detail retrieved successfully");
    }

    @PutMapping("/request/{rideId}/withdraw")
    public RestEntity<Void> withdrawRequest(@PathVariable String rideId, @RequestParam String userId) {
        rideUserMappingService.withdrawRequest(rideId, userId);
        return successResponse("Request withdrawn successfully");
    }

    @PatchMapping("/{rideId}/pool-price")
    public RestEntity<Void> updatePoolPrice(@PathVariable String rideId, @RequestParam String userId, @RequestParam BigDecimal poolPrice) {
        rideService.updatePoolPrice(rideId, userId, poolPrice);
        return successResponse("Pool price updated successfully");
    }

    @GetMapping("/my-rides")
    public RestEntity<List<MyRideResponseDto>> getMyRides(@RequestParam String userId) {
        return successResponse(rideService.getMyRides(userId), "My rides retrieved successfully");
    }

    @GetMapping("/my-rides/{rideId}")
    public RestEntity<MyRideDetailResponseDto> getMyRideDetail(@PathVariable String rideId) {
        log.info("Fetching ride detail for rideId: {} ", rideId);
        return successResponse(rideService.getMyRideDetail(rideId), "Ride detail retrieved successfully");
    }

    @GetMapping("/listing")
    public RestEntity<Page<MyRideResponseDto>> getAvailableRides(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
            @RequestParam(required = false, defaultValue = "15") Integer timeWindow,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam String airportHubId,
            @RequestParam Boolean isFromAirport,
            @RequestParam(required = false) String sourceOrDestinationHubId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeSourceOrDestinationHub,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return successResponse(rideService.getAvailableRides(userId, time, timeWindow, latitude, longitude, airportHubId, isFromAirport, sourceOrDestinationHubId, includeSourceOrDestinationHub, page, size), "Available rides retrieved successfully");
    }
}
