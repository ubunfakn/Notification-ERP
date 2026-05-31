package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.repository.NotificationRepo;
import com.notification_hub.notif.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
}
