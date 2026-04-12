package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.hub.domain.Hub;
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

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    public List<MyRideResponseDto> getAvailableRides(String userId, LocalDateTime time, Integer timeWindow, Double latitude, Double longitude, String airportHubId, Boolean isFromAirport) {
        // 1. Fetch active (SCHEDULED) rides, optionally filtered by time window and hubs
        List<Ride> allActiveRides;
        LocalDateTime minTime = time.minusMinutes(timeWindow);
        LocalDateTime maxTime = time.plusMinutes(timeWindow);

        if (isFromAirport) {
            allActiveRides = rideRepository.findByStatusAndDepartureTimeBetweenAndSourceHubId(RideStatus.SCHEDULED, minTime, maxTime, airportHubId);
        } else {
            allActiveRides = rideRepository.findByStatusAndDepartureTimeBetweenAndDestinationHubId(RideStatus.SCHEDULED, minTime, maxTime, airportHubId);
        }

        if (allActiveRides.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> activeRideIds = allActiveRides.stream()
                .map(Ride::getRideId)
                .collect(Collectors.toSet());

        // 2. Fetch ALL mappings for these rides
        // We fetch all mapping statuses to see if the user has ANY history with the ride,
        // or we can just fetch ACTIVE + PENDING to see if they are currently involved.
        List<RideUserMapping> allMappings = rideUserMappingService.findByRideIdInAndStatusIn(
                activeRideIds,
                Set.of(RideUserMappingStatus.CREATED, RideUserMappingStatus.ACCEPTED, RideUserMappingStatus.PENDING)
        );

        Map<Long, List<RideUserMapping>> mappingsByRideId = allMappings.stream()
                .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        // 3. Filter rides to those where the user is NOT a participant, and seats are available
        List<Ride> availableRides = allActiveRides.stream()
                .filter(ride -> {
                    List<RideUserMapping> pm = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());

                    // User must not be in this list
                    boolean isUserParticipating = pm.stream().anyMatch(m -> m.getUserId().equals(userId));
                    if (isUserParticipating) return false;

                    // Seats must be available (only CREATED and ACCEPTED take up seats)
                    long acceptedCount = pm.stream()
                            .filter(m -> m.getStatus() == RideUserMappingStatus.CREATED || m.getStatus() == RideUserMappingStatus.ACCEPTED)
                            .count();

                    return (ride.getTotalSeats() - acceptedCount) > 0;
                })
                .collect(Collectors.toList());

        if (availableRides.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. Collect needed User IDs and Hub IDs for bulk lookup
        Set<String> participantUserIds = new HashSet<>();
        availableRides.forEach(ride -> {
            List<RideUserMapping> pm = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());
            pm.stream()
                    .filter(m -> m.getStatus() == RideUserMappingStatus.CREATED || m.getStatus() == RideUserMappingStatus.ACCEPTED)
                    .forEach(m -> participantUserIds.add(m.getUserId()));
        });

        Set<Long> hubIds = new HashSet<>();
        availableRides.forEach(ride -> {
            hubIds.add(Long.valueOf(ride.getSourceHubId()));
            hubIds.add(Long.valueOf(ride.getDestinationHubId()));
        });

        // 5. Bulk Fetch User Details and Hubs
        Map<String, UserDetail> userDetailsMap = participantUserIds.isEmpty() ? new HashMap<>() :
                userDetailService.findByUserIdIn(participantUserIds)
                        .stream()
                        .collect(Collectors.toMap(UserDetail::getUserId, ud -> ud));

        Map<Long, Hub> hubsMap = hubService.getHubsMap(hubIds);

        // Sort by distance if latitude and longitude are provided
        if (latitude != null && longitude != null) {
            availableRides.sort((r1, r2) -> {
                Hub targetHub1 = isFromAirport ? hubsMap.get(Long.valueOf(r1.getDestinationHubId())) : hubsMap.get(Long.valueOf(r1.getSourceHubId()));
                double dist1 = targetHub1 != null ? calculateDistance(latitude, longitude, targetHub1.getLatitude(), targetHub1.getLongitude()) : Double.MAX_VALUE;

                Hub targetHub2 = isFromAirport ? hubsMap.get(Long.valueOf(r2.getDestinationHubId())) : hubsMap.get(Long.valueOf(r2.getSourceHubId()));
                double dist2 = targetHub2 != null ? calculateDistance(latitude, longitude, targetHub2.getLatitude(), targetHub2.getLongitude()) : Double.MAX_VALUE;

                return Double.compare(dist1, dist2);
            });
        }

        // 6. Construct the Response
        return availableRides.stream()
                .map(ride -> {
                    List<RideUserMapping> pMapping = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());

                    // Only return accepted participants (CREATED/ACCEPTED)
                    List<RiderInfoDto> participants = pMapping.stream()
                            .filter(pm -> pm.getStatus() == RideUserMappingStatus.CREATED || pm.getStatus() == RideUserMappingStatus.ACCEPTED)
                            .map(pm -> {
                                UserDetail detail = userDetailsMap.get(pm.getUserId());
                                if (detail == null) return null;
                                return RiderInfoDto.Builder.passengerInfoDto()
                                        .withUserId(pm.getUserId())
                                        .withName(detail.getName())
                                        .withImageUrl(detail.getImageUrl())
                                        .withAvgRating(detail.getAvgRating())
                                        .build();
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    int availableSeats = ride.getTotalSeats() - participants.size();

                    Hub srcHub = hubsMap.get(Long.valueOf(ride.getSourceHubId()));
                    Hub destHub = hubsMap.get(Long.valueOf(ride.getDestinationHubId()));

                    return MyRideResponseDto.Builder.myRideResponseDto()
                            .withRideId(ride.getRideId())
                            .withDepartureTime(ride.getDepartureTime())
                            .withSourceHubName(srcHub.getName())
                            .withDestinationHubName(destHub.getName())
                            .withParticipants(participants)
                            .withAvailableSeats(availableSeats)
                            .withPoolPrice(ride.getPoolPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
