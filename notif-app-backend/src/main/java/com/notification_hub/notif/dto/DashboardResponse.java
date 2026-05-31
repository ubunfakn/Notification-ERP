package com.notification_hub.notif.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardResponse {

    private Long totalNotifications;

    private Long sentNotifications;

    private Long failedNotifications;

    private Long retryNotifications;

    private Map<String, StatisticsDto> typeWiseStatistics;

    private Boolean status;
}
