package com.example.server.dashboard.controller;

import com.example.server.dashboard.dto.DashboardResponse;
import com.example.server.dashboard.service.DashboardService;
import com.example.server.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@LoginUser Long userId) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
