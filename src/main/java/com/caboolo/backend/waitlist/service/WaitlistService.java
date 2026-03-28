package com.caboolo.backend.waitlist.service;

import com.caboolo.backend.waitlist.domain.WaitlistEntry;
import com.caboolo.backend.waitlist.repository.WaitlistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator;

    public WaitlistService(WaitlistRepository waitlistRepository,
                           com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator) {
        this.waitlistRepository = waitlistRepository;
        this.sequenceGenerator = sequenceGenerator;
    }

    public void joinWaitlist(String email) {
        if (waitlistRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already on the waitlist");
        }
        WaitlistEntry entry = WaitlistEntry.Builder.waitlistEntry()
                .withWaitlistEntryId(sequenceGenerator.nextId())
                .withEmail(email)
                .build();
        waitlistRepository.save(entry);
    }
}
