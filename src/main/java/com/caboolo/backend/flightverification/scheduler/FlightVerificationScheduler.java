package com.caboolo.backend.flightverification.scheduler;

import com.caboolo.backend.flightverification.domain.FlightVerification;
import com.caboolo.backend.flightverification.enums.VerificationStatus;
import com.caboolo.backend.flightverification.repository.FlightVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightVerificationScheduler {

    private static final long EXPIRY_HOURS = 12L;

    private final FlightVerificationRepository flightVerificationRepository;

    /**
     * Runs every minute. Marks all VERIFIED flight records older than 12 hours as EXPIRED.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireOldVerifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(EXPIRY_HOURS);

        List<FlightVerification> expiredList = flightVerificationRepository
                .findByStatusAndDateCreatedBefore(VerificationStatus.VERIFIED, cutoff);

        if (expiredList.isEmpty()) {
            return;
        }

        expiredList.forEach(fv -> fv.setStatus(VerificationStatus.EXPIRED));
        flightVerificationRepository.saveAll(expiredList);

        log.info("Expired {} flight verification record(s) older than {} hours.", expiredList.size(), EXPIRY_HOURS);
    }
}
