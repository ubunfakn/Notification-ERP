package com.notification_hub.notif.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SchedulerService {

    @Scheduled(fixedRate = 1000)
    public void executeEverySecond() {
        log.info("Scheduler running at {}", LocalDateTime.now());
    }
}

