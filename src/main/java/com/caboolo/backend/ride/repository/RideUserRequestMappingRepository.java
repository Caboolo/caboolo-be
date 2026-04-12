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

    List<RideUserRequestMapping> findByRideIdAndSourceUserId(Long rideId, String sourceUserId);

    Optional<RideUserRequestMapping> findByRideIdAndSourceUserIdAndDestinationUserId(
            Long rideId, String sourceUserId, String destinationUserId);

    List<RideUserRequestMapping> findBySourceUserIdAndStatus(String sourceUserId, RideUserRequestStatus status);

    List<RideUserRequestMapping> findByRideIdAndSourceUserIdAndStatus(
            Long rideId, String sourceUserId, RideUserRequestStatus status);

    boolean existsByRideIdAndSourceUserIdAndStatus(Long rideId, String sourceUserId, RideUserRequestStatus status);

    /**
     * Fetches all request rows for a (rideId, sourceUserId) pair with a pessimistic
     * write lock to prevent concurrent accept/reject decisions from producing
     * duplicate or inconsistent results.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RideUserRequestMapping r WHERE r.rideId = :rideId AND r.sourceUserId = :sourceUserId")
    List<RideUserRequestMapping> findByRideIdAndSourceUserIdWithLock(
            @Param("rideId") Long rideId,
            @Param("sourceUserId") String sourceUserId);
}
