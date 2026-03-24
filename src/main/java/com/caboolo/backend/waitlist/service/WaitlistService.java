package com.caboolo.backend.waitlist.service;

import com.caboolo.backend.waitlist.domain.WaitlistEntry;
import com.caboolo.backend.waitlist.repository.WaitlistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;

    public WaitlistService(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    public void joinWaitlist(String email) {
        if (waitlistRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already on the waitlist");
        }
        WaitlistEntry entry = WaitlistEntry.Builder.waitlistEntry()
                .withEmail(email)
                .build();
        waitlistRepository.save(entry);
    }
}
