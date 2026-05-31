package com.notification_hub.notif.repository;

import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n
            FROM Notification n
            WHERE (:type IS NULL OR n.type = :type)
              AND (:status IS NULL OR n.status = :status)
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
            @Param("keyword") Long userId,
            Pageable pageable
    );

    Optional<Notification> findFirstByUserIdOrderByCreatedAtDesc(@NotNull Long userId);

    Page<Notification> findByStatusAndScheduleTimeLessThanEqual(
            NotificationStatus status,
            LocalDateTime scheduleTime,
            Pageable pageable
    );
}
