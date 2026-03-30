package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.dto.MyRequestResponseDto;
import com.caboolo.backend.ride.dto.RiderInfoDto;
import com.caboolo.backend.ride.dto.MyRideResponseDto;
import com.caboolo.backend.ride.dto.RideRequestDto;
import com.caboolo.backend.ride.enums.RideStatus;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import com.caboolo.backend.ride.repository.RideRepository;
import com.caboolo.backend.userdetails.domain.UserDetail;
import com.caboolo.backend.hub.service.HubService;
import com.caboolo.backend.userdetails.service.UserDetailService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final RideUserMappingService rideUserMappingService;
    private final UserDetailService userDetailService;
    private final HubService hubService;
    private final SequenceGenerator sequenceGenerator;

    public RideService(RideRepository rideRepository, RideUserMappingService rideUserMappingService, UserDetailService userDetailService, HubService hubService, SequenceGenerator sequenceGenerator) {
        this.rideRepository = rideRepository;
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
                .withPoolPrice(request.getPoolPrice())
                .build();

        rideRepository.save(ride);

        // 2. Create and Save RideUserMapping for the creator via the specialized service
        rideUserMappingService.createMapping(rideId, request.getUserId(), RideUserMappingStatus.CREATED);

        return rideId;
    }

    public List<MyRequestResponseDto> getMyRequests(String userId) {
        List<RideUserMapping> allMappings = rideUserMappingService.findByUserIdAndStatus(userId, RideUserMappingStatus.PENDING);
        if (allMappings.isEmpty()) {
            return Collections.emptyList();
        }

        // Separate user's own pending requests from other participants
        List<RideUserMapping> userMappings = allMappings.stream()
                .filter(um -> um.getUserId().equals(userId) && um.getStatus() == RideUserMappingStatus.PENDING)
                .toList();

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


        Map<String, UserDetail> userDetailMap = userDetailService.findByUserIdIn(allActiveUserIds).stream()
                .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        return userMappings.stream()
                .map(um -> {
                    Ride ride = rideMap.get(um.getRideId());
                    if (ride == null) return null;

                    List<RideUserMapping> acceptedMappings = acceptedMappingsByRide.getOrDefault(um.getRideId(), Collections.emptyList());
                    List<RiderInfoDto> activePassengers = acceptedMappings.stream()
                            .map(m -> {
                                UserDetail ud = userDetailMap.get(m.getUserId());
                                return ud == null ? null : RiderInfoDto.Builder.passengerInfoDto()
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

    public List<MyRideResponseDto> getMyRides(String userId) {
        // 1. Find all rides where the user is the "lead" (CREATED status)
        List<RideUserMapping> leadMappings = rideUserMappingService.findByUserIdAndStatus(userId, RideUserMappingStatus.CREATED);
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

        Set<Long> activeRideIds = activeRides.stream()
                .map(Ride::getRideId)
                .collect(Collectors.toSet());

        // 3. Bulk Fetch all participant mappings for these rides
        List<RideUserMapping> allParticipantMappings = rideUserMappingService.findByRideIdInAndStatusIn(
                activeRideIds, RideUserMappingStatus.ACTIVE_STATUSES);

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
                userDetailService.findByUserIdIn(participantUserIds)
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

                    List<RiderInfoDto> participants = pMapping.stream()
                            .map(pm -> {
                                UserDetail detail = userDetailsMap.get(pm.getUserId());
                                return RiderInfoDto.Builder.passengerInfoDto()
                                        .withUserId(pm.getUserId())
                                        .withName(detail.getName())
                                        .withImageUrl(detail.getImageUrl())
                                        .withAvgRating(detail.getAvgRating())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return MyRideResponseDto.Builder.myRideResponseDto()
                            .withRideId(ride.getRideId())
                            .withDepartureTime(ride.getDepartureTime())
                            .withSourceHubName(hubNamesMap.getOrDefault(Long.valueOf(ride.getSourceHubId()), "Unknown Hub"))
                            .withDestinationHubName(hubNamesMap.getOrDefault(Long.valueOf(ride.getDestinationHubId()), "Unknown Hub"))
                            .withParticipants(participants)
                            .withAvailableSeats(ride.getTotalSeats() - allParticipantMappings.size())
                            .withPoolPrice(ride.getPoolPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePoolPrice(Long rideId, String userId, BigDecimal poolPrice) {
        // 2. Fetch and update the ride
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        ride.setPoolPrice(poolPrice);
        rideRepository.save(ride);
    }
}
