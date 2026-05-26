package com.caboolo.backend.flightverification.repository;

import com.caboolo.backend.flightverification.domain.FlightVerification;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

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

    Optional<FlightVerification> findByFlightNumberAndFlightDateAndStatusAndIsDeleted(
            String flightNumber,
            LocalDate flightDate,
            VerificationStatus status,
            boolean isDeleted
    );

    Optional<FlightVerification> findByUserIdAndIsDeleted(String userId, boolean isDeleted);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM FlightVerification f WHERE f.userId IN :userIds AND f.status = :status AND f.isDeleted = false AND f.validFrom <= :currentTime AND f.validUntil >= :currentTime")
    List<FlightVerification> findActiveVerificationsForUsers(
            @Param("userIds") java.util.Collection<String> userIds,
            @Param("status") VerificationStatus status,
            @Param("currentTime") LocalDateTime currentTime
    );
}
