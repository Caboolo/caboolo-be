package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideRepository;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final RideUserMappingService rideUserMappingService;
    private final SequenceGenerator sequenceGenerator;

    @Transactional
    public Long createRide(RideRequestDto request) {
        Long rideId = sequenceGenerator.nextId();
        
        // 1. Create and Save Ride
        Ride ride = Ride.Builder.ride()
                .withRideId(rideId)
                .withSourceHubId(request.getSourceHubId())
                .withDestinationHubId(request.getDestinationHubId())
                .withDepartureTime(request.getDepartureTime())
                .withTotalSeats(request.getTotalSeats())
                .withStatus(RideStatus.SCHEDULED)
                .withIsWomenOnlyRide(request.isWomenOnlyRide())
                .build();
        
        rideRepository.save(ride);

        // 2. Create and Save RideUserMapping for the creator via the specialized service
        rideUserMappingService.createMapping(rideId, request.getUserId(), RideUserMappingStatus.ACCEPTED);

        return rideId;
    }
}
