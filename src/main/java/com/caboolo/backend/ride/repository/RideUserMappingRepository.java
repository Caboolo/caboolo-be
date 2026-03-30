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

    List<RideUserMapping> findByUserId(String userId);

    Optional<RideUserMapping> findByRideIdAndUserId(Long rideId, String userId);

    List<RideUserMapping> findByRideIdIn(List<Long> rideIds);

    @Query(value = "SELECT um FROM ride_user_mapping um WHERE um.rideId IN " +
           "(SELECT rum.rideId FROM ride_user_mapping rum WHERE rum.userId = :userId AND rum.status = :status)", nativeQuery = true)
    List<RideUserMapping> findAllByUserIdAndStatusWithRideParticipants(@Param("userId") String userId, @Param("status") RideUserMappingStatus status);
    List<RideUserMapping> findByUserIdAndStatus(String userId, com.caboolo.backend.ride.enums.RideUserMappingStatus status);
    List<RideUserMapping> findByRideIdAndStatusIn(Long rideId, Collection<com.caboolo.backend.ride.enums.RideUserMappingStatus> statuses);
    List<RideUserMapping> findByRideIdInAndStatusIn(Collection<Long> rideIds, Collection<com.caboolo.backend.ride.enums.RideUserMappingStatus> statuses);
}
