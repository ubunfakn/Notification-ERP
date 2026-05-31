package com.notification_hub.notif.repository;

import com.notification_hub.notif.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
}
