package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.dto.MyRideResponseDto;
import com.caboolo.backend.ride.dto.RideParticipantDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideRepository;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import com.caboolo.backend.hub.service.HubService;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final RideUserMappingRepository rideUserMappingRepository;
    private final RideUserMappingService rideUserMappingService;
    private final UserDetailService userDetailService;
    private final HubService hubService;
    private final SequenceGenerator sequenceGenerator;

    public RideService(RideRepository rideRepository, RideUserMappingRepository rideUserMappingRepository, 
                       RideUserMappingService rideUserMappingService, UserDetailService userDetailService, 
                       HubService hubService, SequenceGenerator sequenceGenerator) {
        this.rideRepository = rideRepository;
        this.rideUserMappingRepository = rideUserMappingRepository;
        this.rideUserMappingService = rideUserMappingService;
        this.userDetailService = userDetailService;
        this.hubService = hubService;
        this.sequenceGenerator = sequenceGenerator;
    }

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
        rideUserMappingService.createMapping(rideId, request.getUserId(), RideUserMappingStatus.CREATED);

        return rideId;
    }

    public List<MyRideResponseDto> getMyRides(String userId) {
        // 1. Find all rides where the user is the "lead" (CREATED status)
        List<RideUserMapping> leadMappings = rideUserMappingRepository.findByUserIdAndStatus(userId, RideUserMappingStatus.CREATED);
        
        List<MyRideResponseDto> myRides = new ArrayList<>();
        
        for (RideUserMapping leadMapping : leadMappings) {
            Long rideId = leadMapping.getRideId();
            
            // 2. Get Ride Details
            rideRepository.findById(rideId).ifPresent(ride -> {
                // 3. Filter for active rides (SCHEDULED or ONGOING)
                if (ride.getStatus() != RideStatus.SCHEDULED && ride.getStatus() != RideStatus.ONGOING) {
                    return;
                }

                // 4. Get all participants for this ride with CREATED or ACCEPTED status
                List<RideUserMapping> participantMappings = rideUserMappingRepository.findByRideIdAndStatusIn(
                        rideId, 
                        Arrays.asList(RideUserMappingStatus.CREATED, RideUserMappingStatus.ACCEPTED)
                );
                
                // 4. Enrich participants with UserDetails
                List<RideParticipantDto> participants = participantMappings.stream()
                        .map(pm -> {
                            try {
                                UserDetail detail = userDetailService.getUserDetailEntity(pm.getUserId());
                                return RideParticipantDto.builder()
                                        .userId(pm.getUserId())
                                        .name(detail.getName())
                                        .avgRating(detail.getAvgRating())
                                        .imageUrl(detail.getImageUrl())
                                        .build();
                            } catch (Exception e) {
                                // Fallback for users without details if necessary
                                return RideParticipantDto.builder()
                                        .userId(pm.getUserId())
                                        .name("Unknown")
                                        .build();
                            }
                        })
                        .collect(Collectors.toList());
                
                myRides.add(MyRideResponseDto.builder()
                        .rideId(rideId)
                        .departureTime(ride.getDepartureTime())
                        .sourceHubName(hubService.getHubName(Long.valueOf(ride.getSourceHubId())))
                        .destinationHubName(hubService.getHubName(Long.valueOf(ride.getDestinationHubId())))
                        .participants(participants)
                        .build());
            });
        }
        
        return myRides;
    }
}
