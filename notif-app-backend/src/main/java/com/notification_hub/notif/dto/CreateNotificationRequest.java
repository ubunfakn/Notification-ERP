package com.notification_hub.notif.dto;

import com.notification_hub.notif.enums.NotificationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNotificationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private NotificationType type;

    @NotBlank
    @Size(max = 200)
    private String message;

    @NotNull
    @FutureOrPresent
    private LocalDateTime scheduleTime;
}
