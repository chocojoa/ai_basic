package com.basic.project.controller;

import com.basic.project.domain.SystemLog;
import com.basic.project.dto.ApiResponse;
import com.basic.project.dto.LogSearchRequest;
import com.basic.project.dto.LogStatsResponse;
import com.basic.project.service.SystemLogService;
import com.basic.project.service.MenuPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class SystemLogController {
    
    private final SystemLogService systemLogService;
    private final MenuPermissionService menuPermissionService;
    
    @GetMapping
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<List<SystemLog>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<SystemLog> logs = systemLogService.getLogsWithPagination(page, size);
            return ResponseEntity.ok(ApiResponse.success("로그 조회 성공", logs));
        } catch (Exception e) {
            log.error("로그 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "로그 조회에 실패했습니다"));
        }
    }
    
    @PostMapping("/search")
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<List<SystemLog>>> searchLogs(@RequestBody LogSearchRequest request) {
        try {
            List<SystemLog> logs = systemLogService.searchLogs(request);
            return ResponseEntity.ok(ApiResponse.success("로그 검색 성공", logs));
        } catch (Exception e) {
            log.error("로그 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "로그 검색에 실패했습니다"));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<List<SystemLog>>> searchLogsGet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            LogSearchRequest request = LogSearchRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .level(level)
                    .username(username)
                    .action(action)
                    .search(search)
                    .page(page)
                    .size(size)
                    .build();
            
            List<SystemLog> logs = systemLogService.searchLogs(request);
            return ResponseEntity.ok(ApiResponse.success("로그 검색 성공", logs));
        } catch (Exception e) {
            log.error("로그 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "로그 검색에 실패했습니다"));
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<LogStatsResponse>> getLogStats() {
        try {
            LogStatsResponse stats = systemLogService.getLogStats();
            return ResponseEntity.ok(ApiResponse.success("로그 통계 조회 성공", stats));
        } catch (Exception e) {
            log.error("로그 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "로그 통계 조회에 실패했습니다"));
        }
    }
    
    @GetMapping("/count")
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<Integer>> getTotalCount() {
        try {
            int count = systemLogService.getTotalCount();
            return ResponseEntity.ok(ApiResponse.success("총 로그 수 조회 성공", count));
        } catch (Exception e) {
            log.error("총 로그 수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "총 로그 수 조회에 실패했습니다"));
        }
    }
    
    @GetMapping("/count/level/{level}")
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public ResponseEntity<ApiResponse<Long>> getCountByLevel(@PathVariable String level) {
        try {
            long count = systemLogService.getCountByLevel(level);
            return ResponseEntity.ok(ApiResponse.success("레벨별 로그 수 조회 성공", count));
        } catch (Exception e) {
            log.error("레벨별 로그 수 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "레벨별 로그 수 조회에 실패했습니다"));
        }
    }
    
    @DeleteMapping("/cleanup")
    @PreAuthorize("@menuPermissionService.canDeleteLogs()")
    public ResponseEntity<ApiResponse<String>> deleteOldLogs(@RequestParam(defaultValue = "30") int days) {
        try {
            systemLogService.deleteOldLogs(days);
            return ResponseEntity.ok(ApiResponse.success("오래된 로그 삭제 성공"));
        } catch (Exception e) {
            log.error("오래된 로그 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "오래된 로그 삭제에 실패했습니다"));
        }
    }
    
    @PostMapping("/test")
    @PreAuthorize("@menuPermissionService.canManageLogs()")
    public ResponseEntity<ApiResponse<String>> createTestLog() {
        try {
            systemLogService.logInfo("admin", "TEST", "테스트 로그 생성", "127.0.0.1", "Test Client");
            return ResponseEntity.ok(ApiResponse.success("테스트 로그 생성 성공"));
        } catch (Exception e) {
            log.error("테스트 로그 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "테스트 로그 생성에 실패했습니다"));
        }
    }
}