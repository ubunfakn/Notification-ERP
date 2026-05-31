package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {

    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private String message;
    private LocalDateTime scheduleTime;
}
