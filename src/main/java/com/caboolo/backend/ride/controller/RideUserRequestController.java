package com.caboolo.backend.ride.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.ride.service.RideUserRequestMappingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ride-request")
public class RideUserRequestController extends BaseController {

    private final RideUserRequestMappingService rideUserRequestMappingService;

    public RideUserRequestController(RideUserRequestMappingService rideUserRequestMappingService) {
        this.rideUserRequestMappingService = rideUserRequestMappingService;
    }

    /**
     * POST /api/v1/ride-request/{rideId}/join?requesterId=...
     *
     * Raises a join request from {@code requesterId} for the given ride.
     * Creates one approval row per active participant and a PENDING placeholder
     * in ride_user_mapping.
     */
    @PostMapping("/{rideId}/join")
    public RestEntity<Void> requestToJoinRide(
            @PathVariable Long rideId,
            @RequestParam String requesterId) {
        rideUserRequestMappingService.requestToJoinRide(rideId, requesterId);
        return successResponse("Join request raised successfully");
    }

    /**
     * PUT /api/v1/ride-request/{rideId}/accept?acceptingUserId=...&requesterId=...
     *
     * Marks the row for this voter as ACCEPTED. If 50% or more of participants
     * have accepted, the requester is promoted to an active ride member.
     * Individual rows for other voters are not modified.
     */
    @PutMapping("/{rideId}/accept")
    public RestEntity<Void> acceptRideRequest(
            @PathVariable Long rideId,
            @RequestParam String acceptingUserId,
            @RequestParam String requesterId) {
        rideUserRequestMappingService.acceptRideRequest(rideId, acceptingUserId, requesterId);
        return successResponse("Ride request accepted");
    }

    /**
     * PUT /api/v1/ride-request/{rideId}/reject?rejectingUserId=...&requesterId=...
     *
     * Marks the row for this voter as REJECTED. If one person in the group has
     * rejected the requester's join request, it is rejected globally.
     * Individual rows for other voters are not modified.
     */
    @PutMapping("/{rideId}/reject")
    public RestEntity<Void> rejectRideRequest(
            @PathVariable Long rideId,
            @RequestParam String rejectingUserId,
            @RequestParam String requesterId) {
        rideUserRequestMappingService.rejectRideRequest(rideId, rejectingUserId, requesterId);
        return successResponse("Ride request rejected");
    }

    /**
     * PUT /api/v1/ride-request/{rideId}/withdraw?requesterId=...
     *
     * Allows the requester to withdraw before a final decision is made.
     * No-op if the request is already in a terminal state.
     */
    @PutMapping("/{rideId}/withdraw")
    public RestEntity<Void> withdrawRideRequest(
            @PathVariable Long rideId,
            @RequestParam String requesterId) {
        rideUserRequestMappingService.withdrawRideRequest(rideId, requesterId);
        return successResponse("Ride request withdrawn");
    }

    /**
     * PUT /api/v1/ride-request/{rideId}/leave?userId=...
     *
     * Allows an accepted ride member to leave the ride.
     */
    @PutMapping("/{rideId}/leave")
    public RestEntity<Void> leaveRide(
            @PathVariable Long rideId,
            @RequestParam String userId) {
        rideUserRequestMappingService.leaveRide(rideId, userId);
        return successResponse("Left the ride successfully");
    }
}
