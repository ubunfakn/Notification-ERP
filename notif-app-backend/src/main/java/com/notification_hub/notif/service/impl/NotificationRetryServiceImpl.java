package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.repository.NotificationRetryRepo;
import com.notification_hub.notif.service.NotificationRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRetryServiceImpl implements NotificationRetryService {

    private final NotificationRetryRepo notificationRetryRepo;
}
