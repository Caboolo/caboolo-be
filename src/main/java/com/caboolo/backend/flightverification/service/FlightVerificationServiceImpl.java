package com.caboolo.backend.flightverification.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.flightverification.client.AviationStackClient;
import com.caboolo.backend.flightverification.client.AviationStackResponse;
import com.caboolo.backend.flightverification.domain.FlightVerification;
import com.caboolo.backend.flightverification.dto.FlightVerificationRequestDto;
import com.caboolo.backend.flightverification.dto.FlightVerificationResponseDto;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import com.caboolo.backend.flightverification.repository.FlightVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class FlightVerificationServiceImpl implements FlightVerificationService {

    private final FlightVerificationRepository flightVerificationRepository;
    private final SequenceGenerator sequenceGenerator;
    private final AviationStackClient aviationStackClient;

    public FlightVerificationServiceImpl(FlightVerificationRepository flightVerificationRepository,
                                         SequenceGenerator sequenceGenerator, AviationStackClient aviationStackClient) {
        this.flightVerificationRepository = flightVerificationRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.aviationStackClient = aviationStackClient;
    }

    @Override
    @Transactional
    public FlightVerificationResponseDto verifyFlight(String userId, FlightVerificationRequestDto request) {
        String flightNumber = request.getFlightNumber();
        LocalDate flightDate = request.getFlightDate();

        // ── Step 1: Short-circuit cache check ─────────────────────────────────
        // If any VERIFIED record already exists for this flight+date (from any user),
        // reuse its timings to avoid an unnecessary AviationStack API call.
        Optional<FlightVerification> cachedVerification = flightVerificationRepository
                .findByFlightNumberAndFlightDateAndStatus(flightNumber, flightDate, VerificationStatus.VERIFIED);

        String departureAirport;
        String arrivalAirport;
        LocalDateTime departureTime;
        LocalDateTime arrivalTime;

        if (cachedVerification.isPresent()) {
            FlightVerification cached = cachedVerification.get();
            departureAirport = cached.getDepartureAirport();
            arrivalAirport = cached.getArrivalAirport();
            departureTime = cached.getDepartureTime();
            arrivalTime = cached.getArrivalTime();
        } else {
            // ── Step 2: Call AviationStack API ────────────────────────────────
            AviationStackResponse response = aviationStackClient.getFlightInfo(
                    flightNumber, flightDate.toString());

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                throw new IllegalArgumentException("Flight not found: " + flightNumber);
            }

            AviationStackResponse.FlightData flightData = response.getData().get(0);

            // ── Step 3: Validate flight date ──────────────────────────────────
            LocalDate apiFlightDate = LocalDate.parse(flightData.getFlightDate());
            if (!apiFlightDate.equals(flightDate)) {
                throw new IllegalArgumentException(
                        "Flight date mismatch: provided " + flightDate + " but flight is on " + apiFlightDate);
            }

            departureAirport = flightData.getDeparture() != null ? flightData.getDeparture().getIata() : null;
            arrivalAirport   = flightData.getArrival()   != null ? flightData.getArrival().getIata()   : null;
            departureTime    = parseScheduled(flightData.getDeparture() != null ? flightData.getDeparture().getScheduled() : null);
            arrivalTime      = parseScheduled(flightData.getArrival()   != null ? flightData.getArrival().getScheduled()   : null);
    }

    // ── Step 4: Delete any existing record for this user ──────────────────
        flightVerificationRepository.findByUserId(userId)
                .ifPresent(existing -> flightVerificationRepository.delete(existing));

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

    // ── Step 6: Build and return response ─────────────────────────────────
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
     * Parses an ISO-8601 offset datetime string (e.g. "2026-04-01T08:00:00+00:00")
     * into a LocalDateTime, or returns null if the input is blank.
     */
    private LocalDateTime parseScheduled(String scheduled) {
        if (scheduled == null || scheduled.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(scheduled).toLocalDateTime();
    }
}
