package com.caboolo.backend.waitlist.controller;

import com.caboolo.backend.waitlist.dto.WaitlistRequestDto;
import com.caboolo.backend.waitlist.service.WaitlistService;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController extends BaseController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping("/join")
    public RestEntity<String> joinWaitlist(@RequestBody WaitlistRequestDto request) {
        log.info("Received request to join waitlist for email: {}", request.getEmail());
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                log.warn("Waitlist join failed: email is null or blank");
                return errorResponse("Email is required", HttpStatus.BAD_REQUEST);
            }
            waitlistService.joinWaitlist(request.getEmail());
            log.info("Email {} successfully joined the waitlist", request.getEmail());
            return successResponse(null, "Successfully joined the waitlist");
        } catch (IllegalArgumentException e) {
            log.warn("Waitlist join failed for {}: {}", request.getEmail(), e.getMessage());
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected error joining waitlist for {}: {}", request.getEmail(), e.getMessage(), e);
            return errorResponse("An error occurred while processing the request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
