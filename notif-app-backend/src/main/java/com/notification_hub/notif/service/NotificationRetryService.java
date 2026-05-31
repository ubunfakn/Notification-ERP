package com.notification_hub.notif.service;

import com.notification_hub.notif.dto.NotificationRetryResponse;

public interface NotificationRetryService {
    NotificationRetryResponse retryNotification(Long id);
}
