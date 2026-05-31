package com.notification_hub.notif.repository;

import com.notification_hub.notif.entity.NotificationRetry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRetryRepo extends JpaRepository<NotificationRetry, Long> {
}
