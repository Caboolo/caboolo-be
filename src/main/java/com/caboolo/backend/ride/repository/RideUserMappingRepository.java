package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideUserMappingRepository extends JpaRepository<RideUserMapping, Long> {

    Optional<RideUserMapping> findByRideIdAndUserId(Long rideId, String userId);
    List<RideUserMapping> findByUserIdAndStatus(String userId, RideUserMappingStatus status);
    List<RideUserMapping> findByRideIdInAndStatusIn(Collection<Long> rideIds, Collection<com.caboolo.backend.ride.enums.RideUserMappingStatus> statuses);
    List<RideUserMapping> findByRideId(Long rideId);
}
