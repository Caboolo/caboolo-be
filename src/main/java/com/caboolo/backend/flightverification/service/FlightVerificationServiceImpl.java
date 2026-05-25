package com.caboolo.backend.flightverification.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.flightverification.client.AeroDataBoxClient;
import com.caboolo.backend.flightverification.client.AeroDataBoxResponse;
import com.caboolo.backend.flightverification.domain.FlightVerification;
import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import com.caboolo.backend.flightverification.repository.FlightVerificationRepository;
import com.caboolo.backend.ride.repository.RideUserMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FlightVerificationServiceImpl implements FlightVerificationService {

    private final FlightVerificationRepository flightVerificationRepository;
    private final SequenceGenerator sequenceGenerator;
    private final AeroDataBoxClient aeroDataBoxClient;
    private final RideUserMappingRepository rideUserMappingRepository;

    public FlightVerificationServiceImpl(FlightVerificationRepository flightVerificationRepository,
                                         SequenceGenerator sequenceGenerator, AeroDataBoxClient aeroDataBoxClient,
                                         RideUserMappingRepository rideUserMappingRepository) {
        this.flightVerificationRepository = flightVerificationRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.aeroDataBoxClient = aeroDataBoxClient;
        this.rideUserMappingRepository = rideUserMappingRepository;
    }

    @Override
    @Transactional
    public FlightVerificationResponseDto verifyFlight(String userId, FlightVerificationRequestDto request) {
        String flightNumber = request.getFlightNumber();
        LocalDate flightDate = request.getFlightDate();
        log.info("Starting flight verification for userId={}, flightNumber={}, flightDate={}", userId, flightNumber, flightDate);

        // ── Step 1: Short-circuit cache check ─────────────────────────────────
        // If any VERIFIED record already exists for this flight+date (from any user),
        // reuse its timings to avoid an unnecessary API call.
        Optional<FlightVerification> cachedVerification = flightVerificationRepository
                .findByFlightNumberAndFlightDateAndStatusAndIsDeleted(flightNumber, flightDate, VerificationStatus.VERIFIED, false);

        String departureAirport;
        String arrivalAirport;
        LocalDateTime departureTime;
        LocalDateTime arrivalTime;

        if (cachedVerification.isPresent()) {
            log.info("Cache hit: reusing existing VERIFIED record for flightNumber={}, flightDate={}", flightNumber, flightDate);
            FlightVerification cached = cachedVerification.get();
            departureAirport = cached.getDepartureAirport();
            arrivalAirport = cached.getArrivalAirport();
            departureTime = cached.getDepartureTime();
            arrivalTime = cached.getArrivalTime();
        } else {
            // ── Step 2: Call AeroDataBox API ────────────────────────────────
            log.info("Cache miss: calling AeroDataBox API for flightNumber={}, flightDate={}", flightNumber, flightDate);
            List<AeroDataBoxResponse> flightList = aeroDataBoxClient.getFlightInfo(flightNumber, flightDate.toString());

            if (flightList == null || flightList.isEmpty()) {
                log.error("Flight not found in AeroDataBox for flightNumber={}, flightDate={}", flightNumber, flightDate);
                throw new IllegalArgumentException("Flight not found: " + flightNumber);
            }

            AeroDataBoxResponse flightData = flightList.get(0);

            // ── Step 3: Validate flight date ──────────────────────────────────
            String scheduledLocal = flightData.getDeparture() != null && flightData.getDeparture().getScheduledTime() != null ? flightData.getDeparture().getScheduledTime().getLocal() : null;
            if (scheduledLocal != null) {
                LocalDate apiFlightDate = LocalDate.parse(scheduledLocal.substring(0, 10));
                if (!apiFlightDate.equals(flightDate)) {
                    log.error("Flight date mismatch for flightNumber={}: provided={}, actual={}", flightNumber, flightDate, apiFlightDate);
                    throw new IllegalArgumentException(
                            "Flight date mismatch: provided " + flightDate + " but flight is on " + apiFlightDate);
                }
            }

            departureAirport = flightData.getDeparture() != null && flightData.getDeparture().getAirport() != null ? flightData.getDeparture().getAirport().getIata() : null;
            arrivalAirport   = flightData.getArrival()   != null && flightData.getArrival().getAirport()   != null ? flightData.getArrival().getAirport().getIata()   : null;
            departureTime    = parseScheduled(scheduledLocal);
            arrivalTime      = parseScheduled(flightData.getArrival() != null && flightData.getArrival().getScheduledTime() != null ? flightData.getArrival().getScheduledTime().getLocal() : null);
        }

        // ── Step 4: Delete any existing record for this user ──────────────────
        flightVerificationRepository.findByUserIdAndIsDeleted(userId, false)
                .ifPresent(existing -> {
                    log.info("Soft-deleting previous flight verification record for userId={}", userId);
                    existing.setDeleted(true);
                    flightVerificationRepository.save(existing);
                });

        // ── Step 4a: Unverify the user's ride mapping ────────────────────────
        rideUserMappingRepository.findByRideIdAndUserId(request.getRideId(), userId)
                .ifPresent(mapping -> {
                    log.info("Unmarking flight verified for rideId={}, userId={}", request.getRideId(), userId);
                    mapping.setFlightVerified(false);
                    rideUserMappingRepository.save(mapping);
                });

        // ── Step 5: Save new VERIFIED record ──────────────────────────────────
        FlightVerification newVerification = FlightVerification.Builder.flightVerification()
                .withFlightVerificationId(sequenceGenerator.nextId())
                .withUserId(userId)
                .withFlightNumber(flightNumber)
                .withFlightDate(flightDate)
                .withDepartureAirport(departureAirport)
                .withArrivalAirport(arrivalAirport)
                .withDepartureTime(departureTime)
                .withArrivalTime(arrivalTime)
                .withStatus(VerificationStatus.VERIFIED)
                .build();

        FlightVerification saved = flightVerificationRepository.save(newVerification);
        log.info("Saved flight verification record for userId={}, flightNumber={}, verificationId={}",
                userId, flightNumber, saved.getFlightVerificationId());

        // ── Step 6: Mark ride user mapping as flight verified ─────────────────
        rideUserMappingRepository.findByRideIdAndUserId(request.getRideId(), userId)
                .ifPresent(mapping -> {
                    log.info("Marking flight verified for rideId={}, userId={}", request.getRideId(), userId);
                    mapping.setFlightVerified(true);
                    rideUserMappingRepository.save(mapping);
                });

        // ── Step 7: Build and return response ─────────────────────────────────
        return FlightVerificationResponseDto.Builder.flightVerificationResponseDto()
                .withFlightVerificationId(saved.getFlightVerificationId())
                .withUserId(saved.getUserId())
                .withFlightNumber(saved.getFlightNumber())
                .withFlightDate(saved.getFlightDate())
                .withDepartureAirport(saved.getDepartureAirport())
                .withArrivalAirport(saved.getArrivalAirport())
                .withDepartureTime(saved.getDepartureTime())
                .withArrivalTime(saved.getArrivalTime())
                .withStatus(saved.getStatus())
                .build();
    }

    /**
     * Parses an ISO-8601 offset datetime string (e.g. "2026-05-23 06:10+05:30")
     * into a LocalDateTime, or returns null if the input is blank.
     */
    private LocalDateTime parseScheduled(String scheduled) {
        if (scheduled == null || scheduled.isBlank()) {
            return null;
        }
        // Normalize space to 'T' for OffsetDateTime parsing: "2026-05-23 06:10+05:30" -> "2026-05-23T06:10+05:30"
        String normalized = scheduled.replace(" ", "T");
        return OffsetDateTime.parse(normalized).toLocalDateTime();
    }
}
