package com.caboolo.backend.flightverification.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlightVerificationResponseDto extends GenericEntityDto {

    private String flightVerificationId;
    private String userId;
    private String flightNumber;
    private LocalDate flightDate;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private VerificationStatus status;

    // ─── Step Builder ──────────────────────────────────────────────────────────

    public interface FlightVerificationIdStep {
        UserIdStep withFlightVerificationId(String flightVerificationId);
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
        FlightVerificationResponseDto build();
    }

    public static class Builder implements FlightVerificationIdStep, UserIdStep, FlightNumberStep,
            FlightDateStep, DepartureAirportStep, ArrivalAirportStep, DepartureTimeStep,
            ArrivalTimeStep, StatusStep, BuildStep {

        private String flightVerificationId;
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

        public static FlightVerificationIdStep flightVerificationResponseDto() {
            return new Builder();
        }

        @Override
        public UserIdStep withFlightVerificationId(String flightVerificationId) {
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
        public FlightVerificationResponseDto build() {
            return new FlightVerificationResponseDto(
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
