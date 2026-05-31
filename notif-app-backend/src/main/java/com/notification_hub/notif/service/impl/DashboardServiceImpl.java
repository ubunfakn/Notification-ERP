package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.repository.NotificationRepo;
import com.notification_hub.notif.repository.NotificationRetryRepo;
import com.notification_hub.notif.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final NotificationRetryRepo notificationRetryRepo;
    private final NotificationRepo notificationRepo;
}
