package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RideUserMappingRepository extends JpaRepository<RideUserMapping, Long> {
    List<RideUserMapping> findByUserIdAndStatus(String userId, com.caboolo.backend.ride.enums.RideUserMappingStatus status);
    List<RideUserMapping> findByRideIdAndStatusIn(Long rideId, Collection<com.caboolo.backend.ride.enums.RideUserMappingStatus> statuses);
}
