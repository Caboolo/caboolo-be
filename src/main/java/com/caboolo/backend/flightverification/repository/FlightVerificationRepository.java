package com.caboolo.backend.flightverification.repository;

import com.caboolo.backend.flightverification.domain.FlightVerification;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightVerificationRepository extends JpaRepository<FlightVerification, Long> {

    Optional<FlightVerification> findByUserId(String userId);

    Optional<FlightVerification> findByFlightNumberAndFlightDateAndStatus(
            String flightNumber,
            LocalDate flightDate,
            VerificationStatus status
    );

    List<FlightVerification> findByStatusAndDateCreatedBefore(
            VerificationStatus status,
            LocalDateTime cutoff
    );
}
