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
    public void createMapping(Long rideId, String userId, RideUserMappingStatus status) {
        Long mappingId = sequenceGenerator.nextId();
        
        RideUserMapping mapping = RideUserMapping.Builder.rideUserMapping()
                .withRideUserMappingId(mappingId)
                .withRideId(rideId)
                .withUserId(userId)
                .withStatus(status)
                .build();
        
        rideUserMappingRepository.save(mapping);
    }

    @Transactional
    public void withdrawRequest(Long rideId, String userId) {
        Optional<RideUserMapping> mappingOpt = rideUserMappingRepository.findByRideIdAndUserId(rideId, userId);
        if (mappingOpt.isEmpty()) {
            throw new RuntimeException("Ride request not found");
        }
        
        RideUserMapping mapping = mappingOpt.get();
        if (mapping.getStatus() != RideUserMappingStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be withdrawn");
        }
        
        mapping.setStatus(RideUserMappingStatus.WITHDRAWN);
        rideUserMappingRepository.save(mapping);
    }

    public List<RideUserMapping> findByUserIdAndStatus(String userId, RideUserMappingStatus rideUserMappingStatus) {
        return rideUserMappingRepository.findByUserIdAndStatus(userId, rideUserMappingStatus);
    }

    public List<RideUserMapping> findByRideIdInAndStatusIn(Set<Long> activeRideIds, Set<RideUserMappingStatus> rideUserMappingStatuses) {
        return rideUserMappingRepository.findByRideIdInAndStatusIn(activeRideIds, rideUserMappingStatuses);
    }

    public List<RideUserMapping> findByRideId(Long rideId) {
        return rideUserMappingRepository.findByRideId(rideId);
    }
}
