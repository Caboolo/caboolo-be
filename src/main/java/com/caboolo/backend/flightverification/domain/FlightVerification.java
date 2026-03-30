package com.caboolo.backend.flightverification.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_verification")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlightVerification extends GenericIdEntity {

    @Column(name = "flight_verification_id")
    private Long flightVerificationId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    @Column(name = "departure_airport")
    private String departureAirport;

    @Column(name = "arrival_airport")
    private String arrivalAirport;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerificationStatus status;

    // ─── Step Builder ──────────────────────────────────────────────────────────

    public interface FlightVerificationIdStep {
        UserIdStep withFlightVerificationId(Long flightVerificationId);
    }

    public interface UserIdStep {
        FlightNumberStep withUserId(String userId);
    }

    public interface FlightNumberStep {
        FlightDateStep withFlightNumber(String flightNumber);
    }

    public interface FlightDateStep {
        DepartureAirportStep withFlightDate(LocalDate flightDate);
    }

    public interface DepartureAirportStep {
        ArrivalAirportStep withDepartureAirport(String departureAirport);
    }

    public interface ArrivalAirportStep {
        DepartureTimeStep withArrivalAirport(String arrivalAirport);
    }

    public interface DepartureTimeStep {
        ArrivalTimeStep withDepartureTime(LocalDateTime departureTime);
    }

    public interface ArrivalTimeStep {
        StatusStep withArrivalTime(LocalDateTime arrivalTime);
    }

    public interface StatusStep {
        BuildStep withStatus(VerificationStatus status);
    }

    public interface BuildStep {
        FlightVerification build();
    }

    public static class Builder implements FlightVerificationIdStep, UserIdStep, FlightNumberStep,
            FlightDateStep, DepartureAirportStep, ArrivalAirportStep, DepartureTimeStep,
            ArrivalTimeStep, StatusStep, BuildStep {

        private Long flightVerificationId;
        private String userId;
        private String flightNumber;
        private LocalDate flightDate;
        private String departureAirport;
        private String arrivalAirport;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private VerificationStatus status;

        private Builder() {
        }

        public static FlightVerificationIdStep flightVerification() {
            return new Builder();
        }

        @Override
        public UserIdStep withFlightVerificationId(Long flightVerificationId) {
            this.flightVerificationId = flightVerificationId;
            return this;
        }

        @Override
        public FlightNumberStep withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public FlightDateStep withFlightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        @Override
        public DepartureAirportStep withFlightDate(LocalDate flightDate) {
            this.flightDate = flightDate;
            return this;
        }

        @Override
        public ArrivalAirportStep withDepartureAirport(String departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        @Override
        public DepartureTimeStep withArrivalAirport(String arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
            return this;
        }

        @Override
        public ArrivalTimeStep withDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        @Override
        public StatusStep withArrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        @Override
        public BuildStep withStatus(VerificationStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public FlightVerification build() {
            return new FlightVerification(
                    this.flightVerificationId,
                    this.userId,
                    this.flightNumber,
                    this.flightDate,
                    this.departureAirport,
                    this.arrivalAirport,
                    this.departureTime,
                    this.arrivalTime,
                    this.status
            );
        }
    }
}
