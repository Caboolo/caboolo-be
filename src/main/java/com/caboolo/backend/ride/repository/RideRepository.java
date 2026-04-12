package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRideIdIn(List<Long> rideIds);
    List<Ride> findByStatusAndRideIdIn(RideStatus status, Collection<Long> rideIds);

    @Query(value = "SELECT r.* FROM ride r " +
            "JOIN hub h ON " +
            "  (:isFromAirport = true AND CAST(r.destination_hub_id AS UNSIGNED) = h.hub_id) " +
            "  OR " +
            "  (:isFromAirport = false AND CAST(r.source_hub_id AS UNSIGNED) = h.hub_id) " +
            "WHERE r.status = :#{#status.name()} " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND ( " +
            "    (:isFromAirport = true AND r.source_hub_id = :airportHubId) " +
            "    OR " +
            "    (:isFromAirport = false AND r.destination_hub_id = :airportHubId) " +
            "  ) " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY ST_Distance_Sphere(POINT(h.longitude, h.latitude), POINT(:longitude, :latitude)) ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :#{#status.name()} " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND ( " +
            "    (:isFromAirport = true AND r.source_hub_id = :airportHubId) " +
            "    OR " +
            "    (:isFromAirport = false AND r.destination_hub_id = :airportHubId) " +
            "  ) " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesSortedByDistanceAndPaginated(
            @Param("status") RideStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("isFromAirport") boolean isFromAirport,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("userId") String userId,
            Pageable pageable);

    @Query(value = "SELECT r.* FROM ride r " +
            "WHERE r.status = :#{#status.name()} " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND ( " +
            "    (:isFromAirport = true AND r.source_hub_id = :airportHubId AND r.destination_hub_id = :sourceOrDestinationHubId) " +
            "    OR " +
            "    (:isFromAirport = false AND r.destination_hub_id = :airportHubId AND r.source_hub_id = :sourceOrDestinationHubId) " +
            "  ) " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY r.departure_time ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :#{#status.name()} " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND ( " +
            "    (:isFromAirport = true AND r.source_hub_id = :airportHubId AND r.destination_hub_id = :sourceOrDestinationHubId) " +
            "    OR " +
            "    (:isFromAirport = false AND r.destination_hub_id = :airportHubId AND r.source_hub_id = :sourceOrDestinationHubId) " +
            "  ) " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesByExactHubsAndPaginated(
            @Param("status") RideStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("isFromAirport") boolean isFromAirport,
            @Param("sourceOrDestinationHubId") String sourceOrDestinationHubId,
            @Param("userId") String userId,
            Pageable pageable);
}
