package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.*;
import com.notification_hub.notif.entity.Notification;
import com.notification_hub.notif.enums.NotificationStatus;
import com.notification_hub.notif.exception.ResourceNotFoundException;
import com.notification_hub.notif.repository.NotificationRepo;
import com.notification_hub.notif.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final NotificationValidationService notificationValidationService;

    @Override
    public NotificationResponse createOrScheduleNotif(CreateNotificationRequest request) {

        if (request == null) {
            throw new RuntimeException("Failed to save Notification as Body is null");
        }

        log.info("Validating Notification");
        notificationValidationService.validateDuplicateNotification(request);

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .message(request.getMessage())
                .scheduleTime(request.getScheduleTime())
                .status(NotificationStatus.PENDING)
                .version(0L)
                .totalRetries(0)
                .build();

        Notification savedNotification = notificationRepo.save(notification);
        log.info("Notification saved successfully with id {}", savedNotification.getId());

        return modelMapper.map(savedNotification, NotificationResponse.class);
    }

    @Override
    public NotificationPageResponse getAllFilteredNotifications(NotificationFilterRequest request) {
        if(request == null){
            request = new NotificationFilterRequest();
        }
        log.info("Preparing Search Object");
        Pageable pageable = PageRequest.of(
                        request.getPage(),
                        request.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
        String keyword = request.getKeyword();
        Long userId = null;

        if (keyword != null && keyword.matches("\\d+")) {
            userId = Long.parseLong(keyword);
        }
        Page<Notification> notifications =
                notificationRepo.findFilteredNotifications(
                        request.getType(),
                        request.getStatus(),
                        request.getKeyword(),
                        userId,
                        pageable
                );
        log.info("{} Notifications fetched successfully", notifications.getNumberOfElements());
        return NotificationPageResponse.builder()
                .notifications(notifications.getContent().stream().map(notification ->
                    this.modelMapper.map(notification, NotificationResponse.class)
                ).toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .numberOfElements(notifications.getNumberOfElements())
                .status(true)
                .build();
    }

    @Override
    public NotificationResponse getNotifById(Long id) {

        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found with id: " + id));
        log.info("Notification fetched with id {}", id);
        return modelMapper.map(notification, NotificationResponse.class);
    }

    @Override
    public NotificationResponse updateNotif(Long id, CreateNotificationRequest request) {

        Notification notification = notificationRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found with id: " + id));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new RuntimeException("Cannot update notification with status: " + notification.getStatus());
        }

        notificationValidationService.validateDuplicateNotification(request);

        int updated = notificationRepo.updateNotification(
                id,
                request.getUserId(),
                request.getType(),
                request.getMessage(),
                request.getScheduleTime()
        );

        if (updated == 0) {
            throw new RuntimeException("Update failed, notification may have been processed");
        }

        return modelMapper.map(
                notificationRepo.findById(id).orElseThrow(),
                NotificationResponse.class
        );
    }

    @Override
    public ApiResponse deleteNotifById(Long id) {

        if (!notificationRepo.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Notification not found with id: " + id);
        }

        log.info("Notification found with id {} ", id);

        notificationRepo.deleteById(id);
        log.info("Notification deleted successfully");
        return ApiResponse.builder()
                .status(true)
                .message("Notification deleted successfully")
                .build();
    }
}
