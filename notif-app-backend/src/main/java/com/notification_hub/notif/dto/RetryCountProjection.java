package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationType;

public interface RetryCountProjection {

    NotificationType getType();

    Long getRetryCount();
}
