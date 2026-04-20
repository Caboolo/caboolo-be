package com.caboolo.backend.ride.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.hub.domain.Hub;
import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.dto.*;
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

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public RideService(RideRepository rideRepository, RideUserMappingService rideUserMappingService,
                       UserDetailService userDetailService, HubService hubService,
                       SequenceGenerator sequenceGenerator) {
        this.rideRepository = rideRepository;
        this.rideUserMappingService = rideUserMappingService;
        this.userDetailService = userDetailService;
        this.hubService = hubService;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional
    public String createRide(RideRequestDto request) throws Exception {
        String rideId = sequenceGenerator.nextId();
        log.info("Creating ride rideId={}, userId={}, sourceHubId={}, destinationHubId={}, departureTime={}",
            rideId, request.getUserId(), request.getSourceHubId(), request.getDestinationHubId(),
            request.getDepartureTime());

        if (StringUtils.equals(request.getSourceHubId(), request.getDestinationHubId())) {
            throw new Exception("Ride cannot be created as source and destination hubs are same.");
        }

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
        rideUserMappingService.createMapping(rideId, request.getUserId(), RideUserMappingStatus.CREATED, null);
        log.info("Ride created successfully: rideId={}", rideId);

        return rideId;
    }

    public List<MyRequestResponseDto> getMyRequests(String userId) {
        List<RideUserMapping> allMappings = rideUserMappingService.findByUserIdAndStatus(userId,
            RideUserMappingStatus.PENDING);
        if (allMappings.isEmpty()) {
            return Collections.emptyList();
        }

        // Separate user's own pending requests from other participants
        List<RideUserMapping> userMappings = allMappings.stream()
            .filter(um -> um.getUserId().equals(userId) && um.getStatus() == RideUserMappingStatus.PENDING)
            .toList();

        List<String> rideIds = userMappings.stream()
            .map(RideUserMapping::getRideId)
            .distinct()
            .collect(Collectors.toList());

        List<Ride> rides = rideRepository.findByRideIdIn(rideIds);
        Map<String, Ride> rideMap = rides.stream()
            .collect(Collectors.toMap(Ride::getRideId, ride -> ride));

        // Group all accepted mappings by rideId for available seats and active passengers
        Map<String, List<RideUserMapping>> acceptedMappingsByRide = allMappings.stream()
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

                List<RideUserMapping> acceptedMappings = acceptedMappingsByRide.getOrDefault(um.getRideId(),
                    Collections.emptyList());
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
                    .withSourceHubName(ride.getSourceHubId().toString())
                    .withDestinationHubName(ride.getDestinationHubId().toString())
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
        // 1. Find all rides where the user is involved
        List<RideUserMapping> userMappings = rideUserMappingService.findByUserId(userId);
        if (userMappings.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, RideUserMappingStatus> statusByRideId = userMappings.stream()
            .collect(Collectors.toMap(RideUserMapping::getRideId, RideUserMapping::getStatus));

        List<String> rideIds = userMappings.stream()
            .map(RideUserMapping::getRideId)
            .collect(Collectors.toList());

        // 2. Bulk Fetch Ride Details with status SCHEDULED directly from DB
        List<Ride> activeRides = rideRepository.findByStatusAndRideIdIn(RideStatus.SCHEDULED, rideIds);

        if (activeRides.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> activeRideIds = activeRides.stream()
            .map(Ride::getRideId)
            .collect(Collectors.toSet());

        // 3. Bulk Fetch all participant mappings for these rides
        List<RideUserMapping> allParticipantMappings = rideUserMappingService.findByRideIdInAndStatusIn(
            activeRideIds, RideUserMappingStatus.ACTIVE_STATUSES);

        Map<String, List<RideUserMapping>> mappingsByRideId = allParticipantMappings.stream()
            .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        // 4. Collect all User IDs and Hub IDs for bulk lookup
        Set<String> participantUserIds = allParticipantMappings.stream()
            .map(RideUserMapping::getUserId)
            .collect(Collectors.toSet());

        Set<String> hubIds = new HashSet<>();
        activeRides.forEach(ride -> {
            hubIds.add(ride.getSourceHubId());
            hubIds.add(ride.getDestinationHubId());
        });

        // 5. Bulk Fetch User Details and Hub Names
        Map<String, UserDetail> userDetailsMap =
            userDetailService.findByUserIdIn(participantUserIds)
                .stream()
                .collect(Collectors.toMap(
                    UserDetail::getUserId,
                    ud -> ud
                ));

        Map<String, String> hubNamesMap = hubService.getHubNames(hubIds);

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

                int rideParticipantCount = pMapping.size();

                return MyRideResponseDto.Builder.myRideResponseDto()
                    .withRideId(ride.getRideId())
                    .withDepartureTime(ride.getDepartureTime())
                    .withSourceHubName(hubNamesMap.getOrDefault(ride.getSourceHubId(), "Unknown Hub"))
                    .withDestinationHubName(hubNamesMap.getOrDefault(ride.getDestinationHubId(), "Unknown Hub"))
                    .withParticipants(participants)
                    .withAvailableSeats(ride.getTotalSeats() - rideParticipantCount)
                    .withPoolPrice(ride.getPoolPrice())
                    .withUserStatus(statusByRideId.get(ride.getRideId()))
                    .build();
            })
            .collect(Collectors.toList());
    }

    public MyRideDetailResponseDto getMyRideDetail(String rideId) {
        log.info("Fetching my ride detail for rideId={}", rideId);
        // 1. Fetch common generic ride detail
        RideDetailResponseDto generalDetail = getRideDetail(rideId);

        // 2. Fetch ACTIVE (CREATED/ACCEPTED) + PENDING mappings for this ride
        List<RideUserMapping> allMappings =
            rideUserMappingService.findByRideIdAndStatusIn(rideId, RideUserMappingStatus.VISIBLE_STATUSES);

        // 3. Resolve user details for all participants
        Set<String> allUserIds = allMappings.stream()
            .map(RideUserMapping::getUserId)
            .collect(Collectors.toSet());

        Map<String, UserDetail> userDetailMap = userDetailService.findByUserIdIn(allUserIds).stream()
            .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        Map<String, String> userIdToCommentsMap =
                allMappings.stream()
                        .collect(Collectors.toMap(
                                RideUserMapping::getUserId,
                                RideUserMapping::getComment));

        // 4. Partition into crew (CREATED/ACCEPTED) and pending — status comes from DB
        List<RideParticipantDto> crewMembers = allMappings.stream()
            .filter(m -> RideUserMappingStatus.ACTIVE_STATUSES.contains(m.getStatus()))
            .map(m -> {
                UserDetail ud = userDetailMap.get(m.getUserId());
                if (ud == null) return null;
                return RideParticipantDto.Builder.rideParticipantDto()
                    .withUserId(ud.getUserId())
                    .withName(ud.getName())
                    .withImageUrl(ud.getImageUrl())
                    .withAvgRating(ud.getAvgRating())
                    .withTotalRides(ud.getTotalReviews())
                    .withStatus(m.getStatus())
                    .withComment(userIdToCommentsMap.get(ud.getUserId()))
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<RideParticipantDto> pendingRequests = allMappings.stream()
            .filter(m -> m.getStatus() == RideUserMappingStatus.PENDING)
            .map(m -> {
                UserDetail ud = userDetailMap.get(m.getUserId());
                if (ud == null) return null;
                return RideParticipantDto.Builder.rideParticipantDto()
                    .withUserId(ud.getUserId())
                    .withName(ud.getName())
                    .withImageUrl(ud.getImageUrl())
                    .withAvgRating(ud.getAvgRating())
                    .withTotalRides(ud.getTotalReviews())
                    .withStatus(m.getStatus())
                    .withComment(userIdToCommentsMap.get(ud.getUserId()))
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 5. Build and return response
        return MyRideDetailResponseDto.Builder.myRideDetailResponseDto()
            .withRideId(generalDetail.getRideId())
            .withDepartureTime(generalDetail.getDepartureTime())
            .withSourceHubName(generalDetail.getSourceHubName())
            .withSourceHubLatitude(generalDetail.getSourceHubLatitude())
            .withSourceHubLongitude(generalDetail.getSourceHubLongitude())
            .withDestinationHubName(generalDetail.getDestinationHubName())
            .withDestinationHubLatitude(generalDetail.getDestinationHubLatitude())
            .withDestinationHubLongitude(generalDetail.getDestinationHubLongitude())
            .withPoolPrice(generalDetail.getPoolPrice())
            .withTotalSeats(generalDetail.getTotalSeats())
            .withAvailableSeats(generalDetail.getAvailableSeats())
            .withCrewMembers(crewMembers)
            .withPendingRequests(pendingRequests)
            .build();
    }


    public List<RideParticipantDto> getMyRideInactiveParticipants(String rideId) {
        log.info("Fetching inactive participants for rideId={}", rideId);

        List<RideUserMapping> inactiveMappings =
            rideUserMappingService.findByRideIdAndStatusIn(rideId, RideUserMappingStatus.INACTIVE_STATUSES);

        if (inactiveMappings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> userIdToCommentsMap =
                inactiveMappings.stream()
                        .collect(Collectors.toMap(
                                RideUserMapping::getUserId,
                                RideUserMapping::getComment));

        Set<String> userIds = inactiveMappings.stream()
            .map(RideUserMapping::getUserId)
            .collect(Collectors.toSet());

        Map<String, UserDetail> userDetailMap = userDetailService.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        return inactiveMappings.stream()
            .map(m -> {
                UserDetail ud = userDetailMap.get(m.getUserId());
                if (ud == null) return null;
                return RideParticipantDto.Builder.rideParticipantDto()
                    .withUserId(ud.getUserId())
                    .withName(ud.getName())
                    .withImageUrl(ud.getImageUrl())
                    .withAvgRating(ud.getAvgRating())
                    .withTotalRides(ud.getTotalReviews())
                    .withStatus(m.getStatus())
                    .withComment(userIdToCommentsMap.get(ud.getUserId()))
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public RideDetailResponseDto getRideDetail(String rideId) {
        log.info("Fetching general ride detail for rideId={}", rideId);

        Ride ride = rideRepository.findByRideId(rideId)
            .orElseThrow(() -> {
                log.error("Ride not found for rideId={}", rideId);
                return new RuntimeException("Ride not found: " + rideId);
            });

        // 2. Fetch all active mappings for this ride
        List<RideUserMapping> crewMappings = rideUserMappingService.findByRideIdAndStatusIn(rideId,
            RideUserMappingStatus.ACTIVE_STATUSES);

        Map<String, String> userIdToCommentsMap =
                crewMappings.stream()
                        .collect(Collectors.toMap(
                                RideUserMapping::getUserId,
                                RideUserMapping::getComment));

        // 3. Collect all user IDs for bulk lookup
        Set<String> activeUserIds = crewMappings.stream()
            .map(RideUserMapping::getUserId)
            .collect(Collectors.toSet());

        Map<String, UserDetail> userDetailMap = userDetailService.findByUserIdIn(activeUserIds).stream()
            .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        // 4. Resolve hub details
        Set<String> hubIds = new HashSet<>();
        String sourceHubId = ride.getSourceHubId();
        String destinationHubId = ride.getDestinationHubId();
        hubIds.add(sourceHubId);
        hubIds.add(destinationHubId);
        Map<String, Hub> hubMap = hubService.getHubsByIds(hubIds);

        com.caboolo.backend.hub.domain.Hub sourceHub = hubMap.get(sourceHubId);
        com.caboolo.backend.hub.domain.Hub destHub = hubMap.get(destinationHubId);

        if (sourceHub == null) {
            log.error("Source hub not found for id: {}", sourceHubId);
            throw new RuntimeException("Source hub not found: " + sourceHubId);
        }
        if (destHub == null) {
            log.error("Destination hub not found for id: {}", destinationHubId);
            throw new RuntimeException("Destination hub not found: " + destinationHubId);
        }

        // 6. Build crew member DTOs
        List<RideParticipantDto> crewMembers = crewMappings.stream()
            .map(m -> {
                UserDetail ud = userDetailMap.get(m.getUserId());
                if (ud == null) return null;
                return RideParticipantDto.Builder.rideParticipantDto()
                    .withUserId(ud.getUserId())
                    .withName(ud.getName())
                    .withImageUrl(ud.getImageUrl())
                    .withAvgRating(ud.getAvgRating())
                    .withTotalRides(ud.getTotalReviews())
                    .withStatus(m.getStatus())
                    .withComment(userIdToCommentsMap.get(ud.getUserId()))
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 7. Build response
        int crewCount = crewMappings.size();
        return RideDetailResponseDto.builder()
            .rideId(ride.getRideId())
            .departureTime(ride.getDepartureTime())
            .sourceHubName(sourceHub.getName())
            .sourceHubLatitude(sourceHub.getLatitude())
            .sourceHubLongitude(sourceHub.getLongitude())
            .destinationHubName(destHub.getName())
            .destinationHubLatitude(destHub.getLatitude())
            .destinationHubLongitude(destHub.getLongitude())
            .poolPrice(ride.getPoolPrice())
            .totalSeats(ride.getTotalSeats())
            .availableSeats(ride.getTotalSeats() - crewCount)
            .participants(crewMembers)
            .build();
    }

    public MyRequestDetailResponseDto getMyRequestDetail(String rideId, String userId) {
        log.info("Fetching request detail for rideId={}, userId={}", rideId, userId);
        // 1. Fetch the ride
        Ride ride = rideRepository.findByRideId(rideId)
            .orElseThrow(() -> {
                log.error("Ride not found for rideId={}", rideId);
                return new RuntimeException("Ride not found: " + rideId);
            });

        // 2. Fetch only relevant mappings: current user's mapping + active crew (CREATED/ACCEPTED)
        List<RideUserMapping> relevantMappings = rideUserMappingService.findByRideIdAndUserIdOrStatusIn(
            rideId, userId, RideUserMappingStatus.ACTIVE_STATUSES);

        // 3. Find the current user's request status
        RideUserMappingStatus requestStatus = relevantMappings.stream()
            .filter(m -> m.getUserId().equals(userId))
            .map(RideUserMapping::getStatus)
            .findFirst()
            .orElseThrow(() -> {
                log.error("No mapping found for rideId={}, userId={}", rideId, userId);
                return new RuntimeException("Request not found for this ride");
            });

        // 4. Get crew members (CREATED + ACCEPTED only)
        List<RideUserMapping> crewMappings = relevantMappings.stream()
            .filter(m -> RideUserMappingStatus.ACTIVE_STATUSES.contains(m.getStatus()))
            .toList();

        Set<String> crewUserIds = crewMappings.stream()
            .map(RideUserMapping::getUserId)
            .collect(Collectors.toSet());

        Map<String, String> userIdToCommentsMap =
                crewMappings.stream()
                        .collect(Collectors.toMap(
                                RideUserMapping::getUserId,
                                RideUserMapping::getComment));

        Map<String, UserDetail> userDetailMap = userDetailService.findByUserIdIn(crewUserIds).stream()
            .collect(Collectors.toMap(UserDetail::getUserId, u -> u));

        // 5. Resolve hub details
        Set<String> hubIds = new HashSet<>();
        hubIds.add(ride.getSourceHubId());
        hubIds.add(ride.getDestinationHubId());
        Map<String, Hub> hubMap = hubService.getHubsByIds(hubIds);

        Hub sourceHub = hubMap.get(ride.getSourceHubId());
        Hub destHub = hubMap.get(ride.getDestinationHubId());

        if (sourceHub == null || destHub == null) {
            throw new RuntimeException("Source Hub or Destination Hub not found");
        }
        // 6. Build crew member DTOs
        List<RideParticipantDto> crewMembers = crewMappings.stream()
            .map(m -> {
                UserDetail ud = userDetailMap.get(m.getUserId());
                if (ud == null) return null;
                return RideParticipantDto.Builder.rideParticipantDto()
                    .withUserId(ud.getUserId())
                    .withName(ud.getName())
                    .withImageUrl(ud.getImageUrl())
                    .withAvgRating(ud.getAvgRating())
                    .withTotalRides(ud.getTotalReviews())
                    .withStatus(m.getStatus())
                    .withComment(userIdToCommentsMap.get(m.getUserId()))
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 7. Build response
        int crewCount = crewMappings.size();
        return MyRequestDetailResponseDto.Builder.myRequestDetailResponseDto()
            .withRideId(ride.getRideId())
            .withRequestStatus(requestStatus)
            .withDepartureTime(ride.getDepartureTime())
            .withSourceHubName(sourceHub.getName())
            .withSourceHubLatitude(sourceHub.getLatitude())
            .withSourceHubLongitude(sourceHub.getLongitude())
            .withDestinationHubName(destHub.getName())
            .withDestinationHubLatitude(destHub.getLatitude())
            .withDestinationHubLongitude(destHub.getLongitude())
            .withPoolPrice(ride.getPoolPrice())
            .withTotalSeats(ride.getTotalSeats())
            .withAvailableSeats(ride.getTotalSeats() - crewCount)
            .withParticipants(crewMembers)
            .build();
    }

    public Page<MyRideResponseDto> getAvailableRides(String userId, LocalDateTime time, Integer timeWindow,
                                                     Double latitude, Double longitude, String airportHubId,
                                                     Boolean isFromAirport, String sourceOrDestinationHubId,
                                                     Boolean includeSourceOrDestinationHub, int page, int size) {
        LocalDateTime minTime = time.minusMinutes(timeWindow);
        LocalDateTime maxTime = time.plusMinutes(timeWindow);
        Pageable pageable = PageRequest.of(page, size);

        Page<Ride> ridesPage;
        if (Boolean.TRUE.equals(includeSourceOrDestinationHub) && sourceOrDestinationHubId != null) {
            if (Boolean.TRUE.equals(isFromAirport)) {
                ridesPage = rideRepository.findAvailableRidesFromAirportByExactHubs(
                    RideStatus.SCHEDULED.name(), minTime, maxTime, airportHubId, sourceOrDestinationHubId, userId,
                    pageable);
            } else {
                ridesPage = rideRepository.findAvailableRidesToAirportByExactHubs(
                    RideStatus.SCHEDULED.name(), minTime, maxTime, airportHubId, sourceOrDestinationHubId, userId,
                    pageable);
            }
        } else {
            if (Boolean.TRUE.equals(isFromAirport)) {
                ridesPage = rideRepository.findAvailableRidesFromAirportSortedByDistance(
                    RideStatus.SCHEDULED.name(), minTime, maxTime, airportHubId, latitude, longitude, userId, pageable);
            } else {
                ridesPage = rideRepository.findAvailableRidesToAirportSortedByDistance(
                    RideStatus.SCHEDULED.name(), minTime, maxTime, airportHubId, latitude, longitude, userId, pageable);
            }
        }

        List<Ride> availableRides = ridesPage.getContent();
        if (availableRides.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<String> activeRideIds = availableRides.stream()
            .map(Ride::getRideId)
            .collect(Collectors.toSet());

        // 2. Fetch mappings to get participants for the DTO
        List<RideUserMapping> allMappings = rideUserMappingService.findByRideIdInAndStatusIn(
            activeRideIds,
            Set.of(RideUserMappingStatus.CREATED, RideUserMappingStatus.ACCEPTED, RideUserMappingStatus.PENDING)
        );

        Map<String, List<RideUserMapping>> mappingsByRideId = allMappings.stream()
            .collect(Collectors.groupingBy(RideUserMapping::getRideId));

        // 3. Collect needed User IDs and Hub IDs for bulk lookup
        Set<String> participantUserIds = new HashSet<>();
        availableRides.forEach(ride -> {
            List<RideUserMapping> pm = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());
            pm.stream()
                .filter(m -> m.getStatus() == RideUserMappingStatus.CREATED
                    || m.getStatus() == RideUserMappingStatus.ACCEPTED)
                .forEach(m -> participantUserIds.add(m.getUserId()));
        });

        Set<String> hubIds = new HashSet<>();
        availableRides.forEach(ride -> {
            hubIds.add(ride.getSourceHubId());
            hubIds.add(ride.getDestinationHubId());
        });

        // 4. Bulk Fetch User Details and Hubs
        Map<String, UserDetail> userDetailsMap = participantUserIds.isEmpty() ? new HashMap<>() :
            userDetailService.findByUserIdIn(participantUserIds)
                .stream()
                .collect(Collectors.toMap(UserDetail::getUserId, ud -> ud));

        Map<String, String> hubsMap = hubService.getHubsMap(hubIds);

        // 5. Construct the Response
        List<MyRideResponseDto> dtoList = availableRides.stream()
            .map(ride -> {
                List<RideUserMapping> pMapping = mappingsByRideId.getOrDefault(ride.getRideId(), new ArrayList<>());

                // Only return accepted participants (CREATED/ACCEPTED)
                List<RiderInfoDto> participants = pMapping.stream()
                    .filter(pm -> pm.getStatus() == RideUserMappingStatus.CREATED
                        || pm.getStatus() == RideUserMappingStatus.ACCEPTED)
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

                String srcHubName = hubsMap.get(ride.getSourceHubId());
                String destHubName = hubsMap.get(ride.getDestinationHubId());

                // Derive the current user's status for this ride (null if no mapping exists)
                RideUserMappingStatus userStatus = pMapping.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .map(RideUserMapping::getStatus)
                    .findFirst()
                    .orElse(null);

                return MyRideResponseDto.Builder.myRideResponseDto()
                    .withRideId(ride.getRideId())
                    .withDepartureTime(ride.getDepartureTime())
                    .withSourceHubName(srcHubName)
                    .withDestinationHubName(destHubName)
                    .withParticipants(participants)
                    .withAvailableSeats(availableSeats)
                    .withPoolPrice(ride.getPoolPrice())
                    .withUserStatus(userStatus)
                    .build();
            })
            .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, ridesPage.getTotalElements());
    }

    @Transactional
    public void updatePoolPrice(String rideId, String userId, BigDecimal poolPrice) {
        log.info("Updating pool price for rideId={}, updatedBy={}, newPoolPrice={}", rideId, userId, poolPrice);
        // 2. Fetch and update the ride
        Ride ride = rideRepository.findByRideId(rideId)
            .orElseThrow(() -> {
                log.error("Ride not found for rideId={} during pool price update", rideId);
                return new RuntimeException("Ride not found");
            });
        ride.setPoolPrice(poolPrice);
        rideRepository.save(ride);
        log.info("Pool price updated successfully for rideId={}", rideId);
    }
}
