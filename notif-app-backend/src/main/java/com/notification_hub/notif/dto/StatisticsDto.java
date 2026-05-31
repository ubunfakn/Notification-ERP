package com.notification_hub.notif.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDto {

    private Long totalNotifications;

    private Long sentNotifications;

    private Long failedNotifications;

    private Long retryNotifications;
}
