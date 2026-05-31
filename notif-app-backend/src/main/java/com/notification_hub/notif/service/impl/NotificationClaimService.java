package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClaimService {

    private final NotificationRepo notificationRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)  // ← own TX, commits immediately
    public boolean claim(Long notificationId) {
        int updated = notificationRepo.claimNotification(notificationId);
        return updated > 0;
        // TX commits here before returning
    }
}
