package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideUserMappingRepository extends JpaRepository<RideUserMapping, Long> {
    List<RideUserMapping> findByRideId(String rideId);
    List<RideUserMapping> findByUserId(String userId);
    List<RideUserMapping> findByRideIdAndIsCreatorTrue(String rideId);
}
