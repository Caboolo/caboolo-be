package com.caboolo.backend.about.controller;

import com.caboolo.backend.about.dto.AboutInfoDto;
import com.caboolo.backend.about.service.AboutService;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/about")
public class AboutController extends BaseController {

    private final AboutService aboutService;

    public AboutController(AboutService aboutService) {
        this.aboutService = aboutService;
    }

    @GetMapping("/info")
    public RestEntity<AboutInfoDto> getAboutInfo() {
        log.info("Received request to get about info");
        return successResponse(aboutService.getAboutInfo(), "About info retrieved successfully");
    }
}
