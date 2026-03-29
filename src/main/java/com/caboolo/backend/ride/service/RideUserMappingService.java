package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
