package com.notification_hub.notif.dto;

import lombok.Data;

@Data
public class StatisticsDto {

    private Long totalNotifications;

    private Long sentNotifications;

    private Long failedNotifications;

    private Long retryNotifications;
}
