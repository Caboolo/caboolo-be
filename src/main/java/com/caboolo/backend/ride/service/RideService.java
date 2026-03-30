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
        List<RideUserMapping> allMappings = rideUserMappingRepository.findAllByUserIdAndStatusWithRideParticipants(userId, RideUserMappingStatus.PENDING);
        if (allMappings.isEmpty()) {
            return Collections.emptyList();
        }

        // Separate user's own pending requests from other participants
        List<RideUserMapping> userMappings = allMappings.stream()
                .filter(um -> um.getUserId().equals(userId) && um.getStatus() == RideUserMappingStatus.PENDING)
                .collect(Collectors.toList());

        List<Long> rideIds = userMappings.stream()
                .map(RideUserMapping::getRideId)
                .distinct()
                .collect(Collectors.toList());

        List<Ride> rides = rideRepository.findByRideIdIn(rideIds);
        Map<Long, Ride> rideMap = rides.stream()
                .collect(Collectors.toMap(Ride::getRideId, ride -> ride));

        // Group all accepted mappings by rideId for available seats and active passengers
        Map<Long, List<RideUserMapping>> acceptedMappingsByRide = allMappings.stream()
                .filter(um -> um.getStatus() == RideUserMappingStatus.ACCEPTED)
                .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        Set<String> allActiveUserIds = acceptedMappingsByRide.values().stream()
                .flatMap(List::stream)
                .map(RideUserMapping::getUserId)
                .collect(Collectors.toSet());
        

        Map<String, UserDetail> userDetailMap = userDetailRepository.findAllByUserIdIn(new ArrayList<>(allActiveUserIds)).stream()
                .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        return userMappings.stream()
                .map(um -> {
                    Ride ride = rideMap.get(um.getRideId());
                    if (ride == null) return null;

                    List<RideUserMapping> acceptedMappings = acceptedMappingsByRide.getOrDefault(um.getRideId(), Collections.emptyList());
                    List<PassengerInfoDto> activePassengers = acceptedMappings.stream()
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

                    int acceptedCount = acceptedMappings.size();

                    return MyRequestResponseDto.Builder.myRequestResponseDto()
                            .withRequestStatus(um.getStatus())
                            .withSourceHubName(ride.getSourceHubId())
                            .withDestinationHubName(ride.getDestinationHubId())
                            .withDepartureTime(ride.getDepartureTime())
                            .withActivePassengers(activePassengers)
                            .withAvailableSeats(ride.getTotalSeats() - acceptedCount)
                            .withPoolPrice(ride.getPoolPrice())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
