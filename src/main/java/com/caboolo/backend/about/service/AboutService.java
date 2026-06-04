package com.caboolo.backend.about.service;

import com.caboolo.backend.about.dto.AboutInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AboutService {

    public AboutInfoDto getAboutInfo() {
        log.info("Fetching About Information");
        // Returning static configuration for now. 
        // Later, this could be fetched from application properties or a config server.
        return AboutInfoDto.builder()
                .version("1.0.0")
                .supportEmail("support@caboolo.com")
                .websiteUrl("https://www.caboolo.com")
                .termsOfServiceUrl("https://www.caboolo.com/terms")
                .privacyPolicyUrl("https://www.caboolo.com/privacy")
                .build();
    }
}
