package com.caboolo.backend.flightverification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Internal POJO that maps the AviationStack /flights response.
 * Only the fields we actually use are mapped; everything else is ignored.
 *
 * Example response structure:
 * {
 *   "data": [{
 *     "flight_date": "2026-04-01",
 *     "flight": { "iata": "AI101" },
 *     "departure": { "iata": "DEL", "scheduled": "2026-04-01T08:00:00+00:00" },
 *     "arrival":   { "iata": "BOM", "scheduled": "2026-04-01T10:00:00+00:00" }
 *   }]
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationStackResponse {

    @JsonProperty("data")
    private List<FlightData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightData {

        @JsonProperty("flight_date")
        private String flightDate;

        @JsonProperty("departure")
        private AirportInfo departure;

        @JsonProperty("arrival")
        private AirportInfo arrival;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirportInfo {

        @JsonProperty("iata")
        private String iata;

        /**
         * Scheduled departure/arrival in ISO-8601 offset format,
         * e.g. "2026-04-01T08:00:00+00:00"
         */
        @JsonProperty("scheduled")
        private String scheduled;
    }
}
