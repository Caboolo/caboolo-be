package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.domain.RideUserRequestMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.enums.RideUserRequestStatus;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import com.caboolo.backend.ride.repository.RideUserRequestMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideUserRequestMappingService {

    private final RideUserRequestMappingRepository requestMappingRepository;
    private final RideUserMappingRepository rideUserMappingRepository;
    private final SequenceGenerator sequenceGenerator;

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
            if (RideUserMappingStatus.ACTIVE_STATUSES.contains(status)) {
                throw new RuntimeException("User is already an active member of this ride");
            }
            if (status == RideUserMappingStatus.PENDING) {
                throw new RuntimeException("User already has a pending join request for this ride");
            }
        }

        // Fetch all current active participants
        List<RideUserMapping> activeParticipants = rideUserMappingRepository
                .findByRideIdInAndStatusIn(List.of(rideId), RideUserMappingStatus.ACTIVE_STATUSES);

        if (activeParticipants.isEmpty()) {
            throw new RuntimeException("No active participants found for ride — cannot raise a join request");
        }

        // Create one request row per existing participant
        for (RideUserMapping participant : activeParticipants) {
            RideUserRequestMapping requestRow = RideUserRequestMapping.Builder
                    .rideUserRequestMapping()
                    .withRideUserRequestMappingId(sequenceGenerator.nextId())
                    .withRideId(rideId)
                    .withSourceUserId(requesterId)
                    .withDestinationUserId(participant.getUserId())
                    .withRideUserMappingId(null)
                    .withStatus(RideUserRequestStatus.PENDING)
                    .build();
            requestMappingRepository.save(requestRow);
        }

        // Create placeholder RideUserMapping for requester (PENDING)
        Long mappingId = sequenceGenerator.nextId();
        RideUserMapping placeholderMapping = RideUserMapping.Builder.rideUserMapping()
                .withRideUserMappingId(mappingId)
                .withRideId(rideId)
                .withUserId(requesterId)
                .withStatus(RideUserMappingStatus.PENDING)
                .build();
        rideUserMappingRepository.save(placeholderMapping);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Accept Request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marks the acceptance of {@code requesterId}'s join request by
     * {@code acceptingUserId}.
     * <p>
     * If all destination users have now accepted, the requester's
     * {@link RideUserMapping} is promoted to {@code ACCEPTED}.
     */
    @Transactional
    public void acceptRideRequest(Long rideId, String acceptingUserId, String requesterId) {
        RideUserRequestMapping requestRow = requestMappingRepository
                .findByRideIdAndSourceUserIdAndDestinationUserId(rideId, requesterId, acceptingUserId)
                .orElseThrow(() -> new RuntimeException("Join request row not found"));

        // Idempotency: skip if already handled
        if (requestRow.getStatus() != RideUserRequestStatus.PENDING) {
            return;
        }

        requestRow.setStatus(RideUserRequestStatus.ACCEPTED);
        requestMappingRepository.save(requestRow);

        // Evaluate final decision under a pessimistic lock
        List<RideUserRequestMapping> allRows =
                requestMappingRepository.findByRideIdAndSourceUserIdWithLock(rideId, requesterId);

        boolean allAccepted = allRows.stream()
                .allMatch(r -> r.getStatus() == RideUserRequestStatus.ACCEPTED);

        if (allAccepted) {
            RideUserMapping requesterMapping = rideUserMappingRepository
                    .findByRideIdAndUserId(rideId, requesterId)
                    .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));
            requesterMapping.setStatus(RideUserMappingStatus.ACCEPTED);
            rideUserMappingRepository.save(requesterMapping);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Reject Request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Immediately rejects {@code requesterId}'s join request.
     * <p>
     * One rejection = global rejection: all remaining PENDING rows for this
     * (rideId, requesterId) are set to {@code REJECTED}, and the requester's
     * {@link RideUserMapping} is also marked {@code REJECTED}.
     */
    @Transactional
    public void rejectRideRequest(Long rideId, String rejectingUserId, String requesterId) {
        RideUserRequestMapping requestRow = requestMappingRepository
                .findByRideIdAndSourceUserIdAndDestinationUserId(rideId, requesterId, rejectingUserId)
                .orElseThrow(() -> new RuntimeException("Join request row not found"));

        // Idempotency: skip if already in a terminal state
        if (requestRow.getStatus() != RideUserRequestStatus.PENDING) {
            return;
        }

        requestRow.setStatus(RideUserRequestStatus.REJECTED);
        requestMappingRepository.save(requestRow);

        // Lock & bulk-reject all remaining PENDING rows
        List<RideUserRequestMapping> allRows =
                requestMappingRepository.findByRideIdAndSourceUserIdWithLock(rideId, requesterId);

        allRows.stream()
                .filter(r -> r.getStatus() == RideUserRequestStatus.PENDING)
                .forEach(r -> r.setStatus(RideUserRequestStatus.REJECTED));
        requestMappingRepository.saveAll(allRows);

        // Reject the requester's placeholder mapping
        RideUserMapping requesterMapping = rideUserMappingRepository
                .findByRideIdAndUserId(rideId, requesterId)
                .orElseThrow(() -> new RuntimeException("Requester's RideUserMapping not found"));
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
                .findByRideIdAndSourceUserIdAndStatus(rideId, requesterId, RideUserRequestStatus.PENDING);

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
    }
}
