package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.NotificationMessage;
import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.producer.NotificationProducer;
import com.notification_hub.notif.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final NotificationRepo notificationRepo;
    private final NotificationProducer notificationProducer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatch(Notification notification) {

        log.info("Dispatch initiated for notification id={}",
                notification.getId());

        Notification fresh = notificationRepo
                .findById(notification.getId())
                .orElse(null);

        if (fresh == null) {
            log.warn("Notification id={} not found",
                    notification.getId());
            return;
        }

        if (fresh.getStatus() != NotificationStatus.PROCESSING) {
            log.warn(
                    "Notification id={} skipped. Expected PROCESSING but found {}",
                    fresh.getId(),
                    fresh.getStatus()
            );
            return;
        }

        try {

            int generatedNumber = (int) (Math.random() * 10) + 1;

            log.debug("Generated random number {} for notification id={}",
                    generatedNumber,
                    fresh.getId());

            if (generatedNumber % 3 == 0) {

                fresh.setStatus(NotificationStatus.FAILED);
                notificationRepo.save(fresh);

                log.warn("Notification id={} marked FAILED (simulated failure)",
                        fresh.getId());

                return;
            }

            NotificationMessage message =
                    NotificationMessage.builder()
                            .notificationId(fresh.getId())
                            .userId(fresh.getUserId())
                            .type(fresh.getType())
                            .message(fresh.getMessage())
                            .build();

            log.info("Publishing notification id={} to RabbitMQ",
                    fresh.getId());

            notificationProducer.publish(message);

            fresh.setStatus(NotificationStatus.SENT);
            notificationRepo.save(fresh);

            log.info("Notification id={} successfully SENT",
                    fresh.getId());

        } catch (Exception ex) {

            log.error("Failed to dispatch notification id={}",
                    fresh.getId(),
                    ex);

            try {
                fresh.setStatus(NotificationStatus.FAILED);
                notificationRepo.save(fresh);

                log.info("Notification id={} marked FAILED",
                        fresh.getId());

            } catch (Exception saveEx) {
                log.error(
                        "Failed to update FAILED status for notification id={}",
                        fresh.getId(),
                        saveEx
                );
            }
        }
    }
}