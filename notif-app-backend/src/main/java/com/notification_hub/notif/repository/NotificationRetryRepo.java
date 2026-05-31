package com.notification_hub.notif.repository;

import com.notification_hub.notif.dto.RetryCountProjection;
import com.notification_hub.notif.entity.NotificationRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRetryRepo extends JpaRepository<NotificationRetry, Long> {
    Optional<NotificationRetry> findTopByNotificationIdOrderByRetriedAtDesc(Long id);

    @Query("""
        Select Count(DISTINCT nr.notification.id)
        From NotificationRetry nr
""")
    long countByNotification();

    @Query("""
        SELECT
            nr.notification.type as type,
            COUNT(DISTINCT nr.notification.id) as retryCount
        FROM NotificationRetry nr
        GROUP BY nr.notification.type
    """)
    List<RetryCountProjection> getRetryCountByType();
}
