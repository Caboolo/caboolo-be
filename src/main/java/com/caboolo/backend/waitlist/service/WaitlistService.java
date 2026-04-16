package com.caboolo.backend.waitlist.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.waitlist.domain.WaitlistEntry;
import com.caboolo.backend.waitlist.repository.WaitlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final SequenceGenerator sequenceGenerator;

    public WaitlistService(WaitlistRepository waitlistRepository,
                           com.caboolo.backend.core.idgen.SequenceGenerator sequenceGenerator) {
        this.waitlistRepository = waitlistRepository;
        this.sequenceGenerator = sequenceGenerator;
    }

    public void joinWaitlist(String email) {
        log.info("Waitlist join request received for email={}", email);
        if (waitlistRepository.existsByEmail(email)) {
            log.warn("Duplicate waitlist join attempt for email={}", email);
            throw new IllegalArgumentException("Email is already on the waitlist");
        }
        WaitlistEntry entry = WaitlistEntry.Builder.waitlistEntry()
                .withWaitlistEntryId(sequenceGenerator.nextId())
                .withEmail(email)
                .build();
        waitlistRepository.save(entry);
        log.info("Email={} successfully added to the waitlist", email);
    }
}
