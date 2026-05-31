package com.notification_hub.notif.repository;

import com.notification_hub.notif.dto.TypeWiseStatisticsProjection;
import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT DISTINCT n
        FROM Notification n
        WHERE (:type IS NULL OR n.type = :type)
          AND (
                :status IS NULL
                OR (
                    :status <> com.notification_hub.notif.enums.NotificationStatus.RETRY
                    AND n.status = :status
                )
                OR (
                    :status = com.notification_hub.notif.enums.NotificationStatus.RETRY
                    AND EXISTS (
                        SELECT 1
                        FROM NotificationRetry nr
                        WHERE nr.notification.id = n.id
                    )
                )
              )
          AND (
                :keyword IS NULL
                OR LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR (:userId IS NOT NULL AND n.userId = :userId)
              )
        """)
    Page<Notification> findFilteredNotifications(
            @Param("type") NotificationType type,
            @Param("status") NotificationStatus status,
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            Pageable pageable
    );

    Optional<Notification> findFirstByUserIdOrderByCreatedAtDesc(@NotNull Long userId);

    Page<Notification> findByStatusAndScheduleTimeLessThanEqual(
            NotificationStatus status,
            LocalDateTime scheduleTime,
            Pageable pageable
    );

    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.status = 'PROCESSING', n.version = n.version + 1
    WHERE n.id = :id AND n.status = 'PENDING'
""")
    int claimNotification(@Param("id") Long id);

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Transactional
    @Query("""
    UPDATE Notification n SET
        n.userId = :userId,
        n.type = :type,
        n.message = :message,
        n.scheduleTime = :scheduleTime
    WHERE n.id = :id AND n.status = 'PENDING'
""")
    int updateNotification(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("message") String message,
            @Param("scheduleTime") LocalDateTime scheduleTime
    );

    long countByStatus(NotificationStatus status);

    long count();

    @Query("""
        SELECT
            n.type as type,
            COUNT(n) as totalNotifications,
            SUM(CASE WHEN n.status = com.notification_hub.notif.enums.NotificationStatus.SENT THEN 1 ELSE 0 END) as sentNotifications,
            SUM(CASE WHEN n.status = com.notification_hub.notif.enums.NotificationStatus.FAILED THEN 1 ELSE 0 END) as failedNotifications
        FROM Notification n
        GROUP BY n.type
    """)
    List<TypeWiseStatisticsProjection> getTypeWiseStatistics();
}
