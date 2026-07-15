package com.caboolo.backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Msg91Service {

    @Value("${msg91.auth-key:}")
    private String authKey;

    @Value("${msg91.sender-id:}")
    private String senderId;

    @Value("${msg91.template-id:}")
    private String templateId;

    private final RestTemplate restTemplate;

    public Msg91Service() {
        this.restTemplate = new RestTemplate();
    }

    public boolean sendOtp(String phoneNumber, String otp) {
        if (authKey == null || authKey.isEmpty()) {
            log.warn("MSG91 Auth Key is not configured. Simulating OTP send to {} : {}", phoneNumber, otp);
            return true;
        }

        try {
            // MSG91 Send OTP URL
            String url = "https://control.msg91.com/api/v5/otp?template_id=" + templateId + "&mobile=" + phoneNumber + "&authkey=" + authKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("otp", otp);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("OTP sent successfully to {}", phoneNumber);
                return true;
            } else {
                log.error("Failed to send OTP to {}. Response: {}", phoneNumber, response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending OTP to {}", phoneNumber, e);
            return false;
        }
    }
}
