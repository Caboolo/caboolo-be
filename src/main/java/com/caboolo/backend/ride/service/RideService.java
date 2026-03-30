package com.caboolo.backend.ride.service;

import com.caboolo.backend.hub.domain.Hub;
import com.caboolo.backend.hub.repository.HubRepository;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.PassengerInfoDto;
import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideRepository;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.userdetails.repository.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final RideUserMappingRepository rideUserMappingRepository;
    private final RideUserMappingService rideUserMappingService;
    private final HubRepository hubRepository;
    private final UserDetailRepository userDetailRepository;
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
                .withPoolPrice(request.getPoolPrice())
                .build();
        
        rideRepository.save(ride);

        // 2. Create and Save RideUserMapping for the creator via the specialized service
        rideUserMappingService.createMapping(rideId, request.getUserId(), RideUserMappingStatus.ACCEPTED);

        return rideId;
    }

    public List<MyRequestResponseDto> getMyRequests(String userId) {
        // 1. Get all mappings for this user
        List<RideUserMapping> userMappings = rideUserMappingRepository.findByUserId(userId);
        if (userMappings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Identify unique ride IDs (excluding cases where user is the creator - those are 'My Posts')
        // For simplicity, we show all rides where they have a mapping, but in 'My Requests' tab, 
        // usually we exclude the one they posted. 
        // Let's filter out mappings where they are the creator if we knew who the creator was.
        // Since we don't have createdByUserId, we'll show all.
        List<Long> rideIds = userMappings.stream()
                .map(RideUserMapping::getRideId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Batch fetch Rides
        List<Ride> rides = rideRepository.findByRideIdIn(rideIds);
        Map<Long, Ride> rideMap = rides.stream()
                .collect(Collectors.toMap(Ride::getRideId, r -> r));

        // 4. Batch fetch Source/Destination Hubs
        Set<String> hubNames = new HashSet<>();
        rides.forEach(r -> {
            hubNames.add(r.getSourceHubId());
            hubNames.add(r.getDestinationHubId());
        });
        List<Hub> hubs = hubRepository.findAllByNameIn(new ArrayList<>(hubNames));
        Map<String, String> hubNameMap = hubs.stream()
                .collect(Collectors.toMap(Hub::getName, Hub::getName)); // HubId is actually Name string in Ride entity

        // 5. Batch fetch ALL mappings for these rides to find co-riders
        List<RideUserMapping> allRideMappings = rideUserMappingRepository.findByRideIdIn(rideIds);
        Map<Long, List<RideUserMapping>> rideToMappingsMap = allRideMappings.stream()
                .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        // 6. Batch fetch UserDetails for all active passengers
        Set<String> allActiveUserIds = allRideMappings.stream()
                .filter(m -> m.getStatus() == RideUserMappingStatus.ACCEPTED)
                .map(RideUserMapping::getUserId)
                .collect(Collectors.toSet());
        
        // Find by userId list (we might need a findByUserIdIn in UserDetailRepository)
        // For now, let's assume we fetch them one by one or add a method.
        // I'll add findByUserIdIn to UserDetailRepository in a moment.
        Map<String, UserDetail> userDetailMap = userDetailRepository.findAllByUserIdIn(new ArrayList<>(allActiveUserIds)).stream()
                .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        // 7. Assemble DTOs
        return userMappings.stream()
                .map(um -> {
                    Ride ride = rideMap.get(um.getRideId());
                    if (ride == null) return null;

                    List<RideUserMapping> rideMappings = rideToMappingsMap.getOrDefault(ride.getRideId(), Collections.emptyList());
                    List<PassengerInfoDto> activePassengers = rideMappings.stream()
                            .filter(m -> m.getStatus() == RideUserMappingStatus.ACCEPTED)
                            .map(m -> {
                                UserDetail ud = userDetailMap.get(m.getUserId());
                                return ud == null ? null : PassengerInfoDto.builder()
                                        .userId(ud.getUserId())
                                        .name(ud.getName())
                                        .imageUrl(ud.getImageUrl())
                                        .avgRating(ud.getAvgRating())
                                        .build();
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    int acceptedCount = activePassengers.size();

                    return MyRequestResponseDto.builder()
                            .rideId(ride.getRideId())
                            .sourceHubName(ride.getSourceHubId()) // It stores name as ID currently
                            .destinationHubName(ride.getDestinationHubId())
                            .departureTime(ride.getDepartureTime())
                            .totalSeats(ride.getTotalSeats())
                            .availableSeats(ride.getTotalSeats() - acceptedCount)
                            .poolPrice(ride.getPoolPrice())
                            .requestStatus(um.getStatus())
                            .rideStatus(ride.getStatus())
                            .activePassengers(activePassengers)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
