package com.notification_hub.notif.service.impl;

import com.notification_hub.notif.dto.*;
import com.notification_hub.notif.entity.Notification;
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

        Notification notification =
                modelMapper.map(request, Notification.class);

        Notification savedNotification =
                notificationRepo.save(notification);
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
        log.info("Notification found with id {}", id);

        log.info("Validating notification");
        notificationValidationService.validateDuplicateNotification(request);

        log.info("Preparing object to update");
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setMessage(request.getMessage());
        notification.setScheduleTime(request.getScheduleTime());

        Notification updatedNotification = notificationRepo.save(notification);
        log.info("Notification updated successfully");
        return modelMapper.map(updatedNotification, NotificationResponse.class);
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
