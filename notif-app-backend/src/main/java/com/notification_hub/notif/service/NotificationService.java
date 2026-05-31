package com.notification_hub.notif.service;

import com.notification_hub.notif.dto.*;
import jakarta.validation.Valid;

public interface NotificationService {
    NotificationResponse createOrScheduleNotif(@Valid CreateNotificationRequest request);

    NotificationPageResponse getAllFilteredNotifications(NotificationFilterRequest request);

    NotificationResponse getNotifById(Long id);

    NotificationResponse updateNotif(Long id, @Valid CreateNotificationRequest request);

    ApiResponse deleteNotifById(Long id);
}
