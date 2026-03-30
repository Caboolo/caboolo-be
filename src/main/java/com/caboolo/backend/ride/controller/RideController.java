package com.caboolo.backend.ride.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.service.RideService;
import com.caboolo.backend.ride.service.RideUserMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ride")
@RequiredArgsConstructor
public class RideController extends BaseController {

    private final RideService rideService;
    private final RideUserMappingService rideUserMappingService;

    @PostMapping("/create")
    public RestEntity<Long> createRide(@RequestBody RideRequestDto request) {
        Long rideId = rideService.createRide(request);
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
}
