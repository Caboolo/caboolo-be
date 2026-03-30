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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        if (leadMappings.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> rideIds = leadMappings.stream()
                .map(RideUserMapping::getRideId)
                .collect(Collectors.toList());

        // 2. Bulk Fetch Ride Details with status SCHEDULED directly from DB
        List<Ride> activeRides = rideRepository.findByStatusAndRideIdIn(RideStatus.SCHEDULED, rideIds);

        if (activeRides.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> activeRideIds = activeRides.stream()
                .map(Ride::getRideId)
                .collect(Collectors.toList());

        // 3. Bulk Fetch all participant mappings for these rides
        List<RideUserMapping> allParticipantMappings = rideUserMappingRepository.findByRideIdInAndStatusIn(
                activeRideIds,
                Arrays.asList(RideUserMappingStatus.CREATED, RideUserMappingStatus.ACCEPTED)
        );

        Map<Long, List<RideUserMapping>> mappingsByRideId = allParticipantMappings.stream()
                .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        // 4. Collect all User IDs and Hub IDs for bulk lookup
        Set<String> participantUserIds = allParticipantMappings.stream()
                .map(RideUserMapping::getUserId)
                .collect(Collectors.toSet());

        Set<Long> hubIds = new HashSet<>();
        activeRides.forEach(ride -> {
            hubIds.add(Long.valueOf(ride.getSourceHubId()));
            hubIds.add(Long.valueOf(ride.getDestinationHubId()));
        });

        // 5. Bulk Fetch User Details and Hub Names
        Map<String, UserDetail> userDetailsMap =
                userDetailService.findAllByUserIdIn(participantUserIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UserDetail::getUserId,
                                ud -> ud
                        ));

        Map<Long, String> hubNamesMap = hubService.getHubNames(hubIds);

        // 6. Construct the Response
        return activeRides.stream()
                .map(ride -> {
                    List<RideUserMapping> pMapping = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());

                    List<RideParticipantDto> participants = pMapping.stream()
                            .map(pm -> {
                                UserDetail detail = userDetailsMap.get(pm.getUserId());
                                return RideParticipantDto.builder()
                                        .userId(pm.getUserId())
                                        .name(detail.getName())
                                        .avgRating(detail.getAvgRating())
                                        .imageUrl(detail.getImageUrl())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return MyRideResponseDto.builder()
                            .rideId(ride.getRideId())
                            .departureTime(ride.getDepartureTime())
                            .sourceHubName(hubNamesMap.getOrDefault(Long.valueOf(ride.getSourceHubId()), "Unknown Hub"))
                            .destinationHubName(hubNamesMap.getOrDefault(Long.valueOf(ride.getDestinationHubId()), "Unknown Hub"))
                            .participants(participants)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
