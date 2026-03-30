package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.RideUserMapping;
import com.caboolo.backend.ride.enums.RideUserMappingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideUserMappingRepository extends JpaRepository<RideUserMapping, Long> {

    List<RideUserMapping> findByUserId(String userId);

    Optional<RideUserMapping> findByRideIdAndUserId(Long rideId, String userId);

    List<RideUserMapping> findByRideIdIn(List<Long> rideIds);

    List<RideUserMapping> findByUserIdAndStatus(String userId, RideUserMappingStatus rideUserMappingStatus);

    @Query("SELECT um FROM rideUserMapping um WHERE um.rideId IN " +
           "(SELECT rum.rideId FROM rideUserMapping rum WHERE rum.userId = :userId AND rum.status = :status)")
    List<RideUserMapping> findAllByUserIdAndStatusWithRideParticipants(@Param("userId") String userId, @Param("status") RideUserMappingStatus status);
}
