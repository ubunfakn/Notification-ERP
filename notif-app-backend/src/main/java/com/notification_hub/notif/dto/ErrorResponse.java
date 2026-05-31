package com.notification_hub.notif.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private boolean status;
    private String message;
    private LocalDateTime timestamp;
}