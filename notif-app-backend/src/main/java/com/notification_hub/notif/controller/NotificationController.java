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
        NotificationResponse notificationResponse =
                this.notificationService.createOrScheduleNotif(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationResponse);
    }

    // Get all paginated Notification with filters
    @GetMapping
    public ResponseEntity<NotificationPageResponse> getAllAndFilterNotifications(
            @ModelAttribute NotificationFilterRequest request) {

        NotificationPageResponse response =
                this.notificationService.getAllFilteredNotifications(request);

        return ResponseEntity.ok(response);
    }

    // Get notification by id
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        NotificationResponse notificationResponse =
                this.notificationService.getNotifById(id);
        return ResponseEntity.ok(notificationResponse);
    }

    // Update Notification
    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody CreateNotificationRequest request) {

        NotificationResponse notificationResponse =
                this.notificationService.updateNotif(id, request);
        return ResponseEntity.ok(null);
    }

    // Delete Notification
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable Long id) {

        ApiResponse apiResponse =
                this.notificationService.deleteNotifById(id);
        return ResponseEntity.ok(null);
    }

    // Retry Notification
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationRetryResponse> retryNotification(
            @PathVariable Long id) {

        NotificationRetryResponse notificationRetryResponse =
                this.notificationRetryService.retryNotification(id);
        return ResponseEntity.ok(null);
    }
}