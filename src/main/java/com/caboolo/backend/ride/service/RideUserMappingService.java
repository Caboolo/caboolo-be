package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideUserMappingService {

    private final RideUserMappingRepository rideUserMappingRepository;
    private final SequenceGenerator sequenceGenerator;

    @Transactional
    public void createMapping(String rideId, String userId, RideUserMappingStatus status) {
        String mappingId = sequenceGenerator.nextId();
        log.info("Creating RideUserMapping: mappingId={}, rideId={}, userId={}, status={}", mappingId, rideId, userId, status);
        
        RideUserMapping mapping = RideUserMapping.Builder.rideUserMapping()
                .withRideUserMappingId(mappingId)
                .withRideId(rideId)
                .withUserId(userId)
                .withStatus(status)
                .build();
        
        rideUserMappingRepository.save(mapping);
        log.info("RideUserMapping created successfully: mappingId={}, rideId={}, userId={}", mappingId, rideId, userId);
    }

    @Transactional
    public void withdrawRequest(String rideId, String userId) {
        log.info("Withdrawing ride request for rideId={}, userId={}", rideId, userId);
        Optional<RideUserMapping> mappingOpt = rideUserMappingRepository.findByRideIdAndUserId(rideId, userId);
        if (mappingOpt.isEmpty()) {
            log.error("Ride request not found for rideId={}, userId={}", rideId, userId);
            throw new RuntimeException("Ride request not found");
        }
        
        RideUserMapping mapping = mappingOpt.get();
        if (mapping.getStatus() != RideUserMappingStatus.PENDING) {
            log.error("Cannot withdraw: request is not in PENDING state for rideId={}, userId={}, status={}",
                    rideId, userId, mapping.getStatus());
            throw new RuntimeException("Only pending requests can be withdrawn");
        }
        
        mapping.setStatus(RideUserMappingStatus.WITHDRAWN);
        rideUserMappingRepository.save(mapping);
        log.info("Ride request withdrawn for rideId={}, userId={}", rideId, userId);
    }

    public List<RideUserMapping> findByUserIdAndStatus(String userId, RideUserMappingStatus rideUserMappingStatus) {
        return rideUserMappingRepository.findByUserIdAndStatus(userId, rideUserMappingStatus);
    }

    public List<RideUserMapping> findByRideIdInAndStatusIn(Set<String> activeRideIds, Set<RideUserMappingStatus> rideUserMappingStatuses) {
        return rideUserMappingRepository.findByRideIdInAndStatusIn(activeRideIds, rideUserMappingStatuses);
    }

    public List<RideUserMapping> findByRideId(String rideId) {
        return rideUserMappingRepository.findByRideId(rideId);
    }
}
