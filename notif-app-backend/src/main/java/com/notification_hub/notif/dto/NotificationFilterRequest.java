package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import lombok.Data;

@Data
public class NotificationFilterRequest {

    private Integer page = 0;
    private Integer size = 10;
    private NotificationStatus status;
    private NotificationType type;
    private String keyword;
}