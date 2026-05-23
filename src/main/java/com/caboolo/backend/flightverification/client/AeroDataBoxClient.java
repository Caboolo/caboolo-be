package com.caboolo.backend.flightverification.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AeroDataBoxClient {

    private final RestTemplate restTemplate;

    @Value("${aerodatabox.api-key}")
    private String apiKey;

    @Value("${aerodatabox.base-url}")
    private String baseUrl;

    public List<AeroDataBoxResponse> getFlightInfo(String flightNumber, String flightDate) {
        log.info("Calling AeroDataBox API for flightNumber={}, flightDate={}", flightNumber, flightDate);
        String url = baseUrl + "/flights/number/" + flightNumber + "/" + flightDate;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", "aerodatabox.p.rapidapi.com");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AeroDataBoxResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, AeroDataBoxResponse[].class);
            log.info("AeroDataBox API call succeeded for flightNumber={}, flightDate={}", flightNumber, flightDate);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (RestClientException e) {
            log.error("AeroDataBox API call failed for flightNumber={}, flightDate={}: {}", flightNumber, flightDate, e.getMessage(), e);
            throw e;
        }
    }
}
