package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserRequestMapping;
import com.caboolo.backend.ride.enums.RideUserRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideUserRequestMappingRepository extends JpaRepository<RideUserRequestMapping, Long> {

    List<RideUserRequestMapping> findByRideIdAndRequestorId(Long rideId, String requestorId);

    Optional<RideUserRequestMapping> findByRideIdAndRequestorIdAndApproverId(
            Long rideId, String requestorId, String approverId);

    List<RideUserRequestMapping> findByRequestorIdAndStatus(String requestorId, RideUserRequestStatus status);

    List<RideUserRequestMapping> findByRideIdAndRequestorIdAndStatus(
            Long rideId, String requestorId, RideUserRequestStatus status);

    /**
     * Fetches all request rows for a (rideId, requestorId) pair with a pessimistic
     * write lock to prevent concurrent accept/reject decisions from producing
     * duplicate or inconsistent results.
     */
    @Query("SELECT r FROM RideUserRequestMapping r WHERE r.rideId = :rideId AND r.requestorId = :requestorId")
    List<RideUserRequestMapping> findByRideIdAndRequestorIdWithLock(
            @Param("rideId") Long rideId,
            @Param("requestorId") String requestorId);

}
