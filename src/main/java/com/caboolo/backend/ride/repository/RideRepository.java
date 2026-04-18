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
    List<Ride> findByRideIdIn(List<String> rideIds);
    List<Ride> findByStatusAndRideIdIn(RideStatus status, Collection<String> rideIds);
    Optional<Ride> findByRideId(String rideId);

    @Query(value = "SELECT r.* FROM ride r " +
            "JOIN hub h ON r.destination_hub_id = h.hub_id " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.source_hub_id = :airportHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY ST_Distance_Sphere(POINT(h.longitude, h.latitude), POINT(:longitude, :latitude)) ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.source_hub_id = :airportHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesFromAirportSortedByDistance(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("userId") String userId,
            Pageable pageable);

    @Query(value = "SELECT r.* FROM ride r " +
            "JOIN hub h ON r.source_hub_id = h.hub_id " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.destination_hub_id = :airportHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY ST_Distance_Sphere(POINT(h.longitude, h.latitude), POINT(:longitude, :latitude)) ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.destination_hub_id = :airportHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesToAirportSortedByDistance(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("userId") String userId,
            Pageable pageable);

    @Query(value = "SELECT r.* FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.source_hub_id = :airportHubId " +
            "  AND r.destination_hub_id = :otherHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY r.departure_time ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.source_hub_id = :airportHubId " +
            "  AND r.destination_hub_id = :otherHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesFromAirportByExactHubs(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("otherHubId") String otherHubId,
            @Param("userId") String userId,
            Pageable pageable);

    @Query(value = "SELECT r.* FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.destination_hub_id = :airportHubId " +
            "  AND r.source_hub_id = :otherHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId) " +
            "ORDER BY r.departure_time ASC",
            countQuery = "SELECT count(r.ride_id) FROM ride r " +
            "WHERE r.status = :status " +
            "  AND r.departure_time BETWEEN :startTime AND :endTime " +
            "  AND r.destination_hub_id = :airportHubId " +
            "  AND r.source_hub_id = :otherHubId " +
            "  AND (r.total_seats - (SELECT COUNT(*) FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.status IN ('CREATED', 'ACCEPTED'))) > 0 " +
            "  AND NOT EXISTS (SELECT 1 FROM ride_user_mapping m WHERE m.ride_id = r.ride_id AND m.user_id = :userId)",
            nativeQuery = true)
    Page<Ride> findAvailableRidesToAirportByExactHubs(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("airportHubId") String airportHubId,
            @Param("otherHubId") String otherHubId,
            @Param("userId") String userId,
            Pageable pageable);


}
