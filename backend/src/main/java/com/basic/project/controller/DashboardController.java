package com.basic.project.controller;

import com.basic.project.domain.SystemLog;
import com.basic.project.dto.ApiResponse;
import com.basic.project.dto.DashboardStatsResponse;
import com.basic.project.service.DashboardService;
import com.basic.project.service.SystemLogService;
import com.basic.project.service.MenuPermissionService;
import com.basic.project.enums.MenuCode;
import com.basic.project.annotation.MenuPermissions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SystemLogService systemLogService;
    private final MenuPermissionService menuPermissionService;

    @GetMapping("/stats")
    @ReadDashboard
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        try {
            DashboardStatsResponse stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("대시보드 통계 조회 성공", stats));
        } catch (Exception e) {
            log.error("대시보드 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("500", "대시보드 통계 조회에 실패했습니다"));
        }
    }
    
    @GetMapping("/recent-activities")
    @ReadDashboard
    public ResponseEntity<ApiResponse<List<SystemLog>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // 최근 활동만 제한적으로 조회 (최대 20개)
            int safeLimit = Math.min(limit, 20);
            List<SystemLog> logs = systemLogService.getLogsWithPagination(0, safeLimit);
            return ResponseEntity.ok(ApiResponse.success("최근 활동 조회 성공", logs));
        } catch (Exception e) {
            log.error("최근 활동 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("500", "최근 활동 조회에 실패했습니다"));
        }
    }
}