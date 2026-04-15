package com.caboolo.backend.flightverification.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class AviationStackClient {

    private final RestTemplate restTemplate;

    @Value("${aviationstack.api-key}")
    private String apiKey;

    @Value("${aviationstack.base-url}")
    private String baseUrl;

    /**
     * Fetches flight data from AviationStack for the given IATA flight number and date.
     *
     * @param flightIata  e.g. "AI101"
     * @param flightDate  e.g. "2026-04-01"
     * @return the raw AviationStack response
     */
    public AviationStackResponse getFlightInfo(String flightIata, String flightDate) {
        log.info("Calling AviationStack API for flightIata={}, flightDate={}", flightIata, flightDate);
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/flights")
                .queryParam("access_key", apiKey)
                .queryParam("flight_iata", flightIata)
                .queryParam("flight_date", flightDate)
                .toUriString();

        try {
            AviationStackResponse response = restTemplate.getForObject(url, AviationStackResponse.class);
            log.info("AviationStack API call succeeded for flightIata={}, flightDate={}", flightIata, flightDate);
            return response;
        } catch (RestClientException e) {
            log.error("AviationStack API call failed for flightIata={}, flightDate={}: {}", flightIata, flightDate, e.getMessage(), e);
            throw e;
        }
    }
}
