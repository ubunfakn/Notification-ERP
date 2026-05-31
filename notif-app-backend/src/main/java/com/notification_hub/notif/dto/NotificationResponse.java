package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private LocalDateTime scheduleTime;
    private NotificationStatus status;

    private Integer totalRetries;
    private LocalDateTime lastRetriedAt;
    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<NotificationRetryResponse> retries;
}
