package com.notification_hub.notif.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private String message;

    private Boolean status;
}
