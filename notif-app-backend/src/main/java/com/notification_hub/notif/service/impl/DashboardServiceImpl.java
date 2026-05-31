package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.DashboardResponse;
import com.notification_hub.notif.dto.RetryCountProjection;
import com.notification_hub.notif.dto.StatisticsDto;
import com.notification_hub.notif.dto.TypeWiseStatisticsProjection;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import com.notification_hub.notif.repository.NotificationRepo;
import com.notification_hub.notif.repository.NotificationRetryRepo;
import com.notification_hub.notif.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final NotificationRetryRepo notificationRetryRepo;
    private final NotificationRepo notificationRepo;

    @Override
    public DashboardResponse getDashBoardCounts() {

        long totalNotifications = notificationRepo.count();

        long sentNotifications =
                notificationRepo.countByStatus(NotificationStatus.SENT);

        long failedNotifications =
                notificationRepo.countByStatus(NotificationStatus.FAILED);

        long retryNotifications =
                notificationRetryRepo.countByNotification();

        Map<NotificationType, Long> retryMap =
                notificationRetryRepo.getRetryCountByType()
                        .stream()
                        .collect(Collectors.toMap(
                                RetryCountProjection::getType,
                                RetryCountProjection::getRetryCount
                        ));

        Map<String, StatisticsDto> typeWiseStatistics = new HashMap<>();

        for (TypeWiseStatisticsProjection stat :
                notificationRepo.getTypeWiseStatistics()) {

            StatisticsDto dto = StatisticsDto.builder()
                    .totalNotifications(stat.getTotalNotifications())
                    .sentNotifications(stat.getSentNotifications())
                    .failedNotifications(stat.getFailedNotifications())
                    .retryNotifications(
                            retryMap.getOrDefault(stat.getType(), 0L)
                    )
                    .build();

            typeWiseStatistics.put(
                    stat.getType().name(),
                    dto
            );
        }

        DashboardResponse response = new DashboardResponse();
        response.setTotalNotifications(totalNotifications);
        response.setSentNotifications(sentNotifications);
        response.setFailedNotifications(failedNotifications);
        response.setRetryNotifications(retryNotifications);
        response.setTypeWiseStatistics(typeWiseStatistics);
        response.setStatus(true);

        return response;
    }
}