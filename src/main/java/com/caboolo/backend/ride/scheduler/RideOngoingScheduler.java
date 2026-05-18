package com.caboolo.backend.ride.scheduler;

import com.caboolo.backend.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideOngoingScheduler {

    private final RideService rideService;

    /**
     * Runs every 15 minutes.
     * Marks rides as ONGOING if they are SCHEDULED and
     * their departure time has started (past or present).
     * Also rejects any pending requests for these rides.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void markRidesAsOngoing() {
        log.info("Starting scheduled job: markRidesAsOngoing");
        int ongoingCount = rideService.markRidesAsOngoing();
        log.info("Scheduled job finished. Rides marked ongoing: {}", ongoingCount);
    }
}
