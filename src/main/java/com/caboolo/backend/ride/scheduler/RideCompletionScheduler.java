package com.caboolo.backend.ride.scheduler;

import com.caboolo.backend.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideCompletionScheduler {

    private final RideService rideService;

    /**
     * Runs every 30 minutes.
     * Marks rides as COMPLETED if they are SCHEDULED or ONGOING and
     * 6 hours have passed since their departure time.
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes in milliseconds
    public void markRidesAsCompleted() {
        log.info("Starting scheduled job: markRidesAsCompleted");
        int completedCount = rideService.markRidesAsCompleted();
        log.info("Scheduled job finished. Rides completed: {}", completedCount);
    }
}
