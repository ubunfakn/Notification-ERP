package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.repository.NotificationRepo;
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

    @Scheduled(fixedRate = 1000)
    public void executeEverySecond() {

        int pageNumber = 0;
        Page<Notification> page;

        do {
            page = notificationRepo.findByStatusAndScheduleTimeLessThanEqual(
                    NotificationStatus.PENDING,
                    LocalDateTime.now(),
                    PageRequest.of(pageNumber, BATCH_SIZE)
            );

            page.getContent()
                    .forEach(dispatchService::dispatch);

            pageNumber++;

        } while (page.hasNext());
    }
}