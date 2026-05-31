package com.notification_hub.notif.controller;

import com.notification_hub.notif.dto.*;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.enums.NotificationType;
import com.notification_hub.notif.service.NotificationRetryService;
import com.notification_hub.notif.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRetryService notificationRetryService;
    private final NotificationService notificationService;

    // Create or Schedule Notification
    @PostMapping
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // Get all paginated Notification with filters
    @GetMapping
    public ResponseEntity<NotificationPageResponse> getAllAndFilterNotifications(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(null);
    }

    // Get notification by id
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(null);
    }

    // Update Notification
    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody CreateNotificationRequest request) {

        return ResponseEntity.ok(null);
    }

    // Delete Notification
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable Long id) {

        return ResponseEntity.ok(null);
    }

    // Retry Notification
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationRetryResponse> retryNotification(
            @PathVariable Long id) {

        return ResponseEntity.ok(null);
    }
}