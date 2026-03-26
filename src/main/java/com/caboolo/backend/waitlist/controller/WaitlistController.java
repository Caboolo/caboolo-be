package com.caboolo.backend.waitlist.controller;

import com.caboolo.backend.waitlist.dto.WaitlistRequestDto;
import com.caboolo.backend.waitlist.service.WaitlistService;
import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return errorResponse("Email is required", HttpStatus.BAD_REQUEST);
            }
            waitlistService.joinWaitlist(request.getEmail());
            return successResponse(null, "Successfully joined the waitlist");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return errorResponse("An error occurred while processing the request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
