package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideUserMappingRepository extends JpaRepository<RideUserMapping, Long> {

    Optional<RideUserMapping> findByRideIdAndUserId(String rideId, String userId);
    List<RideUserMapping> findByUserIdAndStatus(String userId, RideUserMappingStatus status);
    List<RideUserMapping> findByRideIdInAndStatusIn(Collection<String> rideIds, Collection<com.caboolo.backend.ride.enums.RideUserMappingStatus> statuses);
    List<RideUserMapping> findByRideId(String rideId);

    @Query("""
        SELECT m FROM RideUserMapping m
        WHERE m.rideId = :rideId
        AND (
            m.userId = :userId
            OR m.status IN :activeStatuses
        )
    """)
    List<RideUserMapping> findRelevantMappings(
        @Param("rideId") String rideId,
        @Param("userId") String userId,
        @Param("activeStatuses") Collection<RideUserMappingStatus> activeStatuses
    );
}
