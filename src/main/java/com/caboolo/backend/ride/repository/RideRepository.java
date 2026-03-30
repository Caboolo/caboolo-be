package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRideIdIn(List<Long> rideIds);
    List<Ride> findByStatusAndRideIdIn(RideStatus status, Collection<Long> rideIds);
}
