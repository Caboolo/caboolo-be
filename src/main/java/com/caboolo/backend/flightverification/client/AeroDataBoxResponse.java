package com.caboolo.backend.flightverification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AeroDataBoxResponse {

    @JsonProperty("departure")
    private AirportInfo departure;

    @JsonProperty("arrival")
    private AirportInfo arrival;

    @JsonProperty("flight")
    private FlightInfo flight;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirportInfo {

        @JsonProperty("airport")
        private AirportDetails airport;

        @JsonProperty("scheduledTime")
        private TimeInfo scheduledTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirportDetails {
        @JsonProperty("iata")
        private String iata;
        
        @JsonProperty("icao")
        private String icao;
        
        @JsonProperty("name")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeInfo {
        @JsonProperty("local")
        private String local;
        @JsonProperty("utc")
        private String utc;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightInfo {
        @JsonProperty("number")
        private String number;
        @JsonProperty("iata")
        private String iata;
    }
}
