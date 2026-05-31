package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.repository.NotificationRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private static final int BATCH_SIZE = 500;

    private final NotificationRepo notificationRepo;
    private final NotificationDispatchService dispatchService;
    private final NotificationClaimService notificationClaimService;

    @Scheduled(cron = "0 * * * * *")
//    @Transactional
    public void executeEveryMinute() {

        log.info("Notification scheduler starting {}", LocalDateTime.now());

        int pageNumber = 0;
        Page<Notification> page;
        int processedCount = 0;

        try {
            do {
                page = notificationRepo.findByStatusAndScheduleTimeLessThanEqual(
                        NotificationStatus.PENDING,
                        LocalDateTime.now().plusSeconds(2),
                        PageRequest.of(pageNumber, BATCH_SIZE)
                );

                log.info("Processing page {} with {} notifications",
                        pageNumber,
                        page.getNumberOfElements());

                page.getContent().forEach(notification -> {
                    try {
                        boolean claimed = notificationClaimService.claim(notification.getId()); // commits immediately

                        if (claimed) {
                            log.info("Claimed notification id={}", notification.getId());
                            dispatchService.dispatch(notification); // REQUIRES_NEW sees PROCESSING ✓
                        } else {
                            log.warn("Notification id={} already claimed", notification.getId());
                        }

                    } catch (Exception ex) {
                        log.error("Error processing notification id={}", notification.getId(), ex);
                    }
                });

                processedCount += page.getNumberOfElements();
                pageNumber++;

            } while (page.hasNext());

            log.info("Scheduler completed. Total notifications scanned={}",
                    processedCount);

        } catch (Exception ex) {
            log.error("Fatal error in notification scheduler", ex);
        }
    }
}