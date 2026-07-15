package com.caboolo.backend.auth.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class TwoFactorOtpProvider implements OtpProvider {

    private final String apiKey;
    private final String otpTemplateName;
    private final RestTemplate restTemplate;

    public TwoFactorOtpProvider(
            @Value("${twofactor.api-key:}") String apiKey,
            @Value("${twofactor.otp-template-name:OTP1}") String otpTemplateName) {
        this.apiKey = apiKey;
        this.otpTemplateName = otpTemplateName;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void sendOtp(String phoneNumber) {
        String url = String.format("https://2factor.in/API/V1/%s/SMS/%s/AUTOGEN/%s", apiKey, phoneNumber, otpTemplateName);
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<TwoFactorResponse> response = restTemplate.getForEntity(url, TwoFactorResponse.class);
            long latency = System.currentTimeMillis() - startTime;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                if ("Success".equalsIgnoreCase(response.getBody().getStatus())) {
                    log.info("Successfully sent OTP to masked phone: ******{}, latency: {} ms",
                            maskPhoneNumber(phoneNumber), latency);
                    return;
                }
            }
            log.error("Failed to send OTP. Status: {}, Latency: {} ms",
                    response.getBody() != null ? response.getBody().getStatus() : "Unknown", latency);
            throw new RuntimeException("Failed to send OTP via provider");

        } catch (ResourceAccessException e) {
            log.error("Network timeout while sending OTP to masked phone: ******{}, latency: {} ms", maskPhoneNumber(phoneNumber), System.currentTimeMillis() - startTime);
            throw new RuntimeException("Timeout while contacting OTP provider", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error while sending OTP to masked phone: ******{}. Error: {}, latency: {} ms", maskPhoneNumber(phoneNumber), e.getStatusCode(), System.currentTimeMillis() - startTime);
            throw new RuntimeException("Error communicating with OTP provider", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending OTP to masked phone: ******{}", maskPhoneNumber(phoneNumber), e);
            throw new RuntimeException("Unexpected error during OTP sending", e);
        }
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        String url = String.format("https://2factor.in/API/V1/%s/SMS/VERIFY3/%s/%s", apiKey, phoneNumber, otp);
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<TwoFactorResponse> response = restTemplate.getForEntity(url, TwoFactorResponse.class);
            long latency = System.currentTimeMillis() - startTime;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                if ("Success".equalsIgnoreCase(response.getBody().getStatus())) {
                    log.info("Successfully verified OTP for masked phone: ******{}, latency: {} ms",
                            maskPhoneNumber(phoneNumber), latency);
                    return true;
                }
            }
            log.warn("Failed to verify OTP. Status: {}, Latency: {} ms",
                    response.getBody() != null ? response.getBody().getStatus() : "Unknown", latency);
            return false;

        } catch (ResourceAccessException e) {
            log.error("Network timeout while verifying OTP for masked phone: ******{}, latency: {} ms", maskPhoneNumber(phoneNumber), System.currentTimeMillis() - startTime);
            throw new RuntimeException("Timeout while contacting OTP provider", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error while verifying OTP for masked phone: ******{}. Error: {}, latency: {} ms", maskPhoneNumber(phoneNumber), e.getStatusCode(), System.currentTimeMillis() - startTime);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while verifying OTP for masked phone: ******{}", maskPhoneNumber(phoneNumber), e);
            throw new RuntimeException("Unexpected error during OTP verification", e);
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }

    @Data
    public static class TwoFactorResponse {
        @JsonProperty("Status")
        private String status;
        
        @JsonProperty("Details")
        private String details;
    }
}
