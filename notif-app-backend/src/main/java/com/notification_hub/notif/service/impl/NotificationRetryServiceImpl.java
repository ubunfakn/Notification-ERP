package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.NotificationRetryResponse;
import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.entity.NotificationRetry;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.exception.ResourceNotFoundException;
import com.notification_hub.notif.repository.NotificationRepo;
import com.notification_hub.notif.repository.NotificationRetryRepo;
import com.notification_hub.notif.service.NotificationRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryServiceImpl implements NotificationRetryService {

    private final NotificationRetryRepo notificationRetryRepo;
    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final NotificationDispatchService notificationDispatchService;

    @Override
    public NotificationRetryResponse retryNotification(Long id) {

        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with id : " + id));
        log.info("Notification found with id {}" ,id);

        // Status must be FAILED
        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException(
                    "Retry allowed only for FAILED notifications");
        }
        log.info("Notification status is FAILED");

        // Retry count must be < 3
        Integer retryCount =
                notification.getTotalRetries() == null ? 0 : notification.getTotalRetries();

        if (retryCount >= 3) {
            throw new IllegalStateException(
                    "Maximum retry limit reached");
        }
        log.info("Retries are in limit");

        // Last retry must be older than 2 minutes
        LocalDateTime now = LocalDateTime.now();

        NotificationRetry lastRetry =
                notificationRetryRepo
                        .findTopByNotificationIdOrderByRetriedAtDesc(id)
                        .orElse(null);

        if (lastRetry != null &&
                !now.isAfter(lastRetry.getRetriedAt().plusMinutes(2))) {

            throw new IllegalStateException(
                    "Retry can only be attempted after 2 minutes");
        }
        log.info("Can attempt retry");

        // Retry execution (simulate sending)
        NotificationRetry retry = NotificationRetry.builder()
                .notification(notification)
                .retriedAt(now)
                .status(NotificationStatus.PENDING)
                .build();
        log.info("Retry object Prepared, Saving with status PENDING");

        NotificationRetry savedRetry =
                notificationRetryRepo.save(retry);

        notification.setTotalRetries(retryCount + 1);
        notificationRepo.save(notification);
        log.info("Retry processed and notification saved with new status {}", notification.getStatus());

        notificationDispatchService.dispatch(notification);
        return modelMapper.map(
                savedRetry,
                NotificationRetryResponse.class
        );
    }
}
