package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.hub.domain.Hub;
import com.caboolo.backend.hub.repository.HubRepository;
import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.PassengerInfoDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
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
        List<RideUserMapping> userMappings = rideUserMappingRepository.findByUserId(userId);
        if (userMappings.isEmpty()) {
            return Collections.emptyList();
        }


        List<Long> rideIds = userMappings.stream()
                .map(RideUserMapping::getRideId)
                .distinct()
                .collect(Collectors.toList());

        List<Ride> rides = rideRepository.findByRideIdIn(rideIds);
        Map<Long, Ride> rideMap = rides.stream()
                .collect(Collectors.toMap(Ride::getRideId, r -> r));

        Set<String> hubNames = new HashSet<>();
        rides.forEach(r -> {
            hubNames.add(r.getSourceHubId());
            hubNames.add(r.getDestinationHubId());
        });
        List<Hub> hubs = hubRepository.findAllByNameIn(new ArrayList<>(hubNames));
        Map<String, String> hubNameMap = hubs.stream()
                .collect(Collectors.toMap(Hub::getName, Hub::getName)); // HubId is actually Name string in Ride entity

        List<RideUserMapping> allRideMappings = rideUserMappingRepository.findByRideIdIn(rideIds);
        Map<Long, List<RideUserMapping>> rideToMappingsMap = allRideMappings.stream()
                .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        Set<String> allActiveUserIds = allRideMappings.stream()
                .filter(m -> m.getStatus() == RideUserMappingStatus.ACCEPTED)
                .map(RideUserMapping::getUserId)
                .collect(Collectors.toSet());
        

        Map<String, UserDetail> userDetailMap = userDetailRepository.findAllByUserIdIn(new ArrayList<>(allActiveUserIds)).stream()
                .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        return userMappings.stream()
                .map(um -> {
                    Ride ride = rideMap.get(um.getRideId());
                    if (ride == null) return null;

                    List<RideUserMapping> rideMappings = rideToMappingsMap.getOrDefault(ride.getRideId(), Collections.emptyList());
                    List<PassengerInfoDto> activePassengers = rideMappings.stream()
                            .filter(m -> m.getStatus() == RideUserMappingStatus.ACCEPTED)
                            .map(m -> {
                                UserDetail ud = userDetailMap.get(m.getUserId());
                                return ud == null ? null : PassengerInfoDto.Builder.passengerInfoDto()
                                        .withUserId(ud.getUserId())
                                        .withName(ud.getName())
                                        .withImageUrl(ud.getImageUrl())
                                        .withAvgRating(ud.getAvgRating())
                                        .build();
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    int acceptedCount = activePassengers.size();

                    return MyRequestResponseDto.Builder.myRequestResponseDto()
                            .withRequestStatus(um.getStatus())
                            .withRideStatus(ride.getStatus())
                            .withActivePassengers(activePassengers)
                            .withAvailableSeats(ride.getTotalSeats() - acceptedCount)
                            .withPoolPrice(ride.getPoolPrice())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
