package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.notification.event.RideNotificationEvent;
import com.caboolo.backend.notification.event.RideNotificationType;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.domain.RideUserRequestMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.enums.RideUserRequestStatus;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import com.caboolo.backend.ride.repository.RideUserRequestMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideUserRequestMappingService {

    private final RideUserRequestMappingRepository requestMappingRepository;
    private final RideUserMappingRepository rideUserMappingRepository;
    private final SequenceGenerator sequenceGenerator;
    private final ApplicationEventPublisher eventPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Request to Join Ride
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Raises a join request for {@code requesterId} on {@code rideId}.
     * <p>
     * One {@link RideUserRequestMapping} row is created per existing active
     * participant, and a PENDING placeholder is inserted in
     * {@link RideUserMapping} for the requester.
     */
    @Transactional
    public void requestToJoinRide(Long rideId, String requesterId) {
        // Guard: user must not already be an active member
        Optional<RideUserMapping> existingActive = rideUserMappingRepository.findByRideIdAndUserId(rideId, requesterId);
        if (existingActive.isPresent()) {
            RideUserMappingStatus status = existingActive.get().getStatus();
            if (!RideUserMappingStatus.PENDING.equals(status)) {
                throw new RuntimeException("No Pending requests for the user");
            }
        }

        // Fetch all current active participants
        List<RideUserMapping> activeParticipants = rideUserMappingRepository
                .findByRideIdInAndStatusIn(List.of(rideId), RideUserMappingStatus.ACTIVE_STATUSES);

        if (activeParticipants.isEmpty()) {
            throw new RuntimeException("No active participants found for ride — cannot raise a join request");
        }

        // Create placeholder RideUserMapping for requester (PENDING)
        Long mappingId = sequenceGenerator.nextId();

        // Create one request row per existing participant
        for (RideUserMapping participant : activeParticipants) {
            RideUserRequestMapping requestRow = RideUserRequestMapping.Builder
                    .rideUserRequestMapping()
                    .withRideUserRequestMappingId(sequenceGenerator.nextId())
                    .withRideId(rideId)
                    .withRequestorId(requesterId)
                    .withApproverId(participant.getUserId())
                    .withRideUserMappingId(mappingId)
                    .withStatus(RideUserRequestStatus.PENDING)
                    .build();
            requestMappingRepository.save(requestRow);
        }

        RideUserMapping placeholderMapping = RideUserMapping.Builder.rideUserMapping()
                .withRideUserMappingId(mappingId)
                .withRideId(rideId)
                .withUserId(requesterId)
                .withStatus(RideUserMappingStatus.PENDING)
                .build();
        rideUserMappingRepository.save(placeholderMapping);

        // Publish event → notify crew members
        List<String> crewUserIds = activeParticipants.stream()
                .map(RideUserMapping::getUserId)
                .collect(Collectors.toList());

        eventPublisher.publishEvent(
                RideNotificationEvent.of(RideNotificationType.RIDE_REQUEST_SENT, rideId, requesterId, crewUserIds)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Accept Request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marks the acceptance of {@code requesterId}'s join request by
     * {@code acceptingUserId}.
     * <p>
     * If 50% or more of the destination users have accepted, the requester's
     * {@link RideUserMapping} is promoted to {@code ACCEPTED}.
     * Individual request rows for other voters are left untouched.
     */
    @Transactional
    public void acceptRideRequest(Long rideId, String acceptingUserId, String requesterId) {
        // Guard: parent mapping must still be PENDING
        RideUserMapping requesterMapping = rideUserMappingRepository
                .findByRideIdAndUserId(rideId, requesterId)
                .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));
        if (requesterMapping.getStatus() != RideUserMappingStatus.PENDING) {
            throw new RuntimeException("Request is no longer pending — cannot accept");
        }

        RideUserRequestMapping requestRow = requestMappingRepository
                .findByRideIdAndRequestorIdAndApproverId(rideId, requesterId, acceptingUserId)
                .orElseThrow(() -> new RuntimeException("Join request row not found"));

        // Idempotency: skip if already handled
        if (requestRow.getStatus() != RideUserRequestStatus.PENDING) {
            return;
        }

        requestRow.setStatus(RideUserRequestStatus.ACCEPTED);
        requestMappingRepository.save(requestRow);

        List<RideUserRequestMapping> allRows =
                requestMappingRepository.findByRideIdAndRequestorIdWithLock(rideId, requesterId);

        long totalVoters = allRows.size();
        long acceptedCount = allRows.stream()
                .filter(r -> r.getStatus() == RideUserRequestStatus.ACCEPTED)
                .count();

        // 50% or more accepted → promote to ACCEPTED globally
        if (acceptedCount * 2 >= totalVoters) {
            RideUserMapping userMapping = rideUserMappingRepository
                    .findByRideIdAndUserId(rideId, requesterId)
                    .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));
            if (userMapping.getStatus() != RideUserMappingStatus.PENDING) {
                throw new RuntimeException("Request is no longer pending");
            }
            userMapping.setStatus(RideUserMappingStatus.ACCEPTED);
            rideUserMappingRepository.save(userMapping);

            // Publish event → notify requester: ride confirmed
            eventPublisher.publishEvent(
                    RideNotificationEvent.of(RideNotificationType.RIDE_CONFIRMED, rideId, requesterId, List.of(requesterId))
            );

            // Publish event → notify crew: new member joined
            List<String> crewUserIds = allRows.stream()
                    .map(RideUserRequestMapping::getApproverId)
                    .distinct()
                    .collect(Collectors.toList());

            eventPublisher.publishEvent(
                    RideNotificationEvent.of(RideNotificationType.MATCH_FOUND, rideId, requesterId, crewUserIds)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Reject Request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Immediately rejects {@code requesterId}'s join request.
     * <p>
     * One rejection = global rejection: the parent row
     * (rideId, requesterId) is set to {@code REJECTED}, and the requester's
     * {@link RideUserMapping} is also marked {@code REJECTED}.
     * The remaining rows remain unaffected.
     */
    @Transactional
    public void rejectRideRequest(Long rideId, String rejectingUserId, String requesterId) {
        // Guard: parent mapping must still be PENDING
        RideUserMapping requesterMapping = rideUserMappingRepository
                .findByRideIdAndUserId(rideId, requesterId)
                .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));
        if (requesterMapping.getStatus() != RideUserMappingStatus.PENDING) {
            throw new RuntimeException("Request is no longer pending — cannot reject");
        }

        RideUserRequestMapping requestRow = requestMappingRepository
                .findByRideIdAndRequestorIdAndApproverId(rideId, requesterId, rejectingUserId)
                .orElseThrow(() -> new RuntimeException("Join request row not found"));

        // Idempotency: skip if already in a terminal state
        if (requestRow.getStatus() != RideUserRequestStatus.PENDING) {
            return;
        }

        requestRow.setStatus(RideUserRequestStatus.REJECTED);
        requestMappingRepository.save(requestRow);

        // Reject the requester's placeholder mapping
        requesterMapping.setStatus(RideUserMappingStatus.REJECTED);
        rideUserMappingRepository.save(requesterMapping);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Withdraw Request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Allows the requester to withdraw their own pending join request.
     * If no PENDING rows exist (already decided), this is a no-op.
     */
    @Transactional
    public void withdrawRideRequest(Long rideId, String requesterId) {
        List<RideUserRequestMapping> pendingRows = requestMappingRepository
                .findByRideIdAndRequestorIdAndStatus(rideId, requesterId, RideUserRequestStatus.PENDING);

        if (pendingRows.isEmpty()) {
            // Already decided or already withdrawn — no-op
            return;
        }

        pendingRows.forEach(r -> r.setStatus(RideUserRequestStatus.WITHDRAWN));
        requestMappingRepository.saveAll(pendingRows);

        // Withdraw the requester's placeholder mapping
        RideUserMapping requesterMapping = rideUserMappingRepository
                .findByRideIdAndUserId(rideId, requesterId)
                .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));

        if (requesterMapping.getStatus() == RideUserMappingStatus.PENDING) {
            requesterMapping.setStatus(RideUserMappingStatus.WITHDRAWN);
            rideUserMappingRepository.save(requesterMapping);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Leave Ride
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marks an active ride member as having left the ride.
     *
     * @throws RuntimeException if the user has no active mapping for the ride.
     */
    @Transactional
    public void leaveRide(Long rideId, String userId) {
        RideUserMapping mapping = rideUserMappingRepository
                .findByRideIdAndUserId(rideId, userId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this ride"));

        if (!RideUserMappingStatus.ACTIVE_STATUSES.contains(mapping.getStatus())) {
            throw new RuntimeException("Only active members can leave a ride");
        }

        mapping.setStatus(RideUserMappingStatus.LEFT);
        rideUserMappingRepository.save(mapping);

        // Publish event → notify remaining crew
        List<RideUserMapping> remainingCrew = rideUserMappingRepository
                .findByRideIdInAndStatusIn(List.of(rideId), RideUserMappingStatus.ACTIVE_STATUSES);

        List<String> crewUserIds = remainingCrew.stream()
                .map(RideUserMapping::getUserId)
                .filter(id -> !id.equals(userId))
                .distinct()
                .collect(Collectors.toList());

        eventPublisher.publishEvent(
                RideNotificationEvent.of(RideNotificationType.MEMBER_LEFT, rideId, userId, crewUserIds)
        );
    }
}
