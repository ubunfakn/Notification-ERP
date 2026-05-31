package com.notification_hub.notif.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationPageResponse {

    private List<NotificationResponse> notifications;

    private Integer page;

    private Integer size;

    private Long totalElements;

    private Integer numberOfElements;

    private Integer totalPages;

    private Boolean status;
}