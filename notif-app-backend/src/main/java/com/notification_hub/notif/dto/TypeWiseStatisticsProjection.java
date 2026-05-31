package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationType;

public interface TypeWiseStatisticsProjection {

    NotificationType getType();

    Long getTotalNotifications();

    Long getSentNotifications();

    Long getFailedNotifications();
}
