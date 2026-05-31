package com.notification_hub.notif.controller;

import com.notification_hub.notif.dto.DashboardResponse;
import com.notification_hub.notif.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardCounts() {

        DashboardResponse dashboardResponse =
                this.dashboardService.getDashBoardCounts();
        return ResponseEntity.ok(null);
    }
}
