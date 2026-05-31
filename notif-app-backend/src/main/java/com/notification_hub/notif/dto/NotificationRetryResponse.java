package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRetryResponse {

    private Long id;
    private LocalDateTime retriedAt;
    private NotificationStatus status;
}
