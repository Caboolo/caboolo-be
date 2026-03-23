package com.caboolo.backend.waitlist.controller;

import com.caboolo.backend.waitlist.dto.WaitlistRequest;
import com.caboolo.backend.waitlist.service.WaitlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinWaitlist(@RequestBody WaitlistRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            waitlistService.joinWaitlist(request.getEmail());
            return ResponseEntity.ok("Successfully joined the waitlist");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while processing the request");
        }
    }
}
