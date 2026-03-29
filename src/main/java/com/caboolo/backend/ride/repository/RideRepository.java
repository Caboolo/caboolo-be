package com.caboolo.backend.ride.repository;

import com.caboolo.backend.ride.domain.Ride;
import com.caboolo.backend.ride.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
}
