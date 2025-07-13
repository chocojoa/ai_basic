package com.basic.project.controller;

import com.basic.project.enums.MenuCode;
import com.basic.project.service.ApiMonitoringService;
import com.basic.project.service.MenuPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.basic.project.dto.ApiResponse;

@Tag(name = "시스템 모니터링", description = "API 성능 및 시스템 상태 모니터링")
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
public class MonitoringController {
    
    private final ApiMonitoringService apiMonitoringService;
    private final MenuPermissionService menuPermissionService;
    private final MetricsEndpoint metricsEndpoint;
    private final HealthEndpoint healthEndpoint;
    
    private static final String NO_MONITORING_PERMISSION_MESSAGE = "시스템 모니터링 권한이 없습니다";
    
    @Operation(summary = "API 통계 정보 조회", description = "전체 API 호출 통계 및 성능 지표를 조회합니다.")
    @GetMapping("/api-statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiStatistics(Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // 시스템 모니터링 권한 확인
            if (!menuPermissionService.hasReadPermission(username, MenuCode.SYSTEM_MONITORING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", NO_MONITORING_PERMISSION_MESSAGE));
            }
            
            Map<String, Object> statistics = apiMonitoringService.getApiStatistics();
            
            return ResponseEntity.ok(ApiResponse.success("API 통계 조회 성공", statistics));
            
        } catch (Exception e) {
            log.error("API 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "API 통계 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상위 느린 API 조회
     */
    @GetMapping("/slow-apis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopSlowApis(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // 시스템 모니터링 권한 확인
            if (!menuPermissionService.hasReadPermission(username, MenuCode.SYSTEM_MONITORING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", NO_MONITORING_PERMISSION_MESSAGE));
            }
            
            Map<String, Object> slowApis = apiMonitoringService.getTopSlowApis(limit);
            
            Map<String, Object> response = Map.of(
                    "slowApis", slowApis,
                    "limit", limit
            );
            return ResponseEntity.ok(ApiResponse.success("느린 API 조회 성공", response));
            
        } catch (Exception e) {
            log.error("느린 API 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "느린 API 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 상위 에러 API 조회
     */
    @GetMapping("/error-apis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopErrorApis(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // 시스템 모니터링 권한 확인
            if (!menuPermissionService.hasReadPermission(username, MenuCode.SYSTEM_MONITORING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", NO_MONITORING_PERMISSION_MESSAGE));
            }
            
            Map<String, Object> errorApis = apiMonitoringService.getTopErrorApis(limit);
            
            Map<String, Object> response = Map.of(
                    "errorApis", errorApis,
                    "limit", limit
            );
            return ResponseEntity.ok(ApiResponse.success("에러 API 조회 성공", response));
            
        } catch (Exception e) {
            log.error("에러 API 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "에러 API 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * API 통계 초기화
     */
    @PostMapping("/reset-statistics")
    public ResponseEntity<ApiResponse<String>> resetApiStatistics(Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // 시스템 모니터링 권한 확인
            if (!menuPermissionService.hasWritePermission(username, MenuCode.SYSTEM_MONITORING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", NO_MONITORING_PERMISSION_MESSAGE));
            }
            
            apiMonitoringService.resetStatistics();
            
            return ResponseEntity.ok(ApiResponse.success("API 통계가 성공적으로 초기화되었습니다"));
            
        } catch (Exception e) {
            log.error("API 통계 초기화 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "API 통계 초기화 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 실시간 시스템 상태 조회 (향상된 버전)
     */
    @GetMapping("/system-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatus(Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // 시스템 모니터링 권한 확인
            if (!menuPermissionService.hasReadPermission(username, MenuCode.SYSTEM_MONITORING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("403", NO_MONITORING_PERMISSION_MESSAGE));
            }
            
            Map<String, Object> systemStatus = new HashMap<>();
            
            // JVM 메모리 정보 (향상된 버전)
            systemStatus.put("jvm", getJvmMetrics());
            
            // 시스템 정보 (향상된 버전)
            systemStatus.put("system", getSystemMetrics());
            
            // 애플리케이션 정보
            systemStatus.put("application", getApplicationMetrics());
            
            // 건강 상태
            systemStatus.put("health", getHealthStatus());
            
            systemStatus.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(ApiResponse.success("시스템 상태 조회 성공", systemStatus));
            
        } catch (Exception e) {
            log.error("시스템 상태 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "시스템 상태 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * JVM 메트릭 수집
     */
    private Map<String, Object> getJvmMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted();
        
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryBean.getNonHeapMemoryUsage().getMax();
        long nonHeapCommitted = memoryBean.getNonHeapMemoryUsage().getCommitted();
        
        Map<String, Object> jvmMetrics = new HashMap<>();
        jvmMetrics.put("maxMemory", formatBytes(heapMax));
        jvmMetrics.put("usedMemory", formatBytes(heapUsed));
        jvmMetrics.put("freeMemory", formatBytes(heapMax - heapUsed));
        jvmMetrics.put("totalMemory", formatBytes(heapCommitted));
        jvmMetrics.put("memoryUsagePercent", String.format("%.2f", (double) heapUsed / heapMax * 100));
        
        // Non-Heap 메모리 (메타스페이스 등)
        jvmMetrics.put("nonHeapUsed", formatBytes(nonHeapUsed));
        jvmMetrics.put("nonHeapMax", nonHeapMax == -1 ? "무제한" : formatBytes(nonHeapMax));
        jvmMetrics.put("nonHeapCommitted", formatBytes(nonHeapCommitted));
        
        // 가비지 컬렉션 정보
        jvmMetrics.put("gcCollections", getGcMetrics());
        
        // JVM 실행 시간
        long uptime = runtimeBean.getUptime();
        jvmMetrics.put("uptime", formatDuration(Duration.ofMillis(uptime)));
        jvmMetrics.put("startTime", runtimeBean.getStartTime());
        
        return jvmMetrics;
    }
    
    /**
     * 시스템 메트릭 수집
     */
    private Map<String, Object> getSystemMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("osName", osBean.getName());
        systemMetrics.put("osVersion", osBean.getVersion());
        systemMetrics.put("osArch", osBean.getArch());
        systemMetrics.put("availableProcessors", osBean.getAvailableProcessors());
        systemMetrics.put("systemLoadAverage", 
            osBean.getSystemLoadAverage() == -1.0 ? "사용 불가" : 
            String.format("%.2f", osBean.getSystemLoadAverage()));
        
        systemMetrics.put("javaVersion", System.getProperty("java.version"));
        systemMetrics.put("javaVendor", System.getProperty("java.vendor"));
        systemMetrics.put("javaHome", System.getProperty("java.home"));
        
        // JVM 인수
        systemMetrics.put("jvmArguments", runtimeBean.getInputArguments().size());
        
        return systemMetrics;
    }
    
    /**
     * 애플리케이션 메트릭 수집
     */
    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> appMetrics = new HashMap<>();
        
        try {
            // Spring Boot Actuator 메트릭 활용
            var httpRequestsMetric = metricsEndpoint.metric("http.server.requests", null);
            if (httpRequestsMetric != null) {
                appMetrics.put("totalHttpRequests", 
                    httpRequestsMetric.getMeasurements().stream()
                        .filter(m -> "COUNT".equals(m.getStatistic().toString()))
                        .mapToDouble(MetricsEndpoint.Sample::getValue)
                        .sum());
            }
            
            // 활성 스레드 수
            appMetrics.put("activeThreads", Thread.activeCount());
            appMetrics.put("peakThreads", ManagementFactory.getThreadMXBean().getPeakThreadCount());
            
        } catch (Exception e) {
            log.warn("애플리케이션 메트릭 수집 중 오류: {}", e.getMessage());
        }
        
        return appMetrics;
    }
    
    /**
     * 건강 상태 확인
     */
    private Map<String, Object> getHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            var health = healthEndpoint.health();
            healthStatus.put("status", health.getStatus().getCode());
            healthStatus.put("description", "애플리케이션 건강 상태");
        } catch (Exception e) {
            healthStatus.put("status", "DOWN");
            healthStatus.put("error", e.getMessage());
            log.warn("건강 상태 확인 중 오류: {}", e.getMessage());
        }
        
        return healthStatus;
    }
    
    /**
     * GC 메트릭 수집
     */
    private Map<String, Object> getGcMetrics() {
        Map<String, Object> gcMetrics = new HashMap<>();
        
        try {
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> 
                gcMetrics.put(gcBean.getName(), Map.of(
                    "collections", gcBean.getCollectionCount(),
                    "time", formatDuration(Duration.ofMillis(gcBean.getCollectionTime()))
                ))
            );
        } catch (Exception e) {
            log.warn("GC 메트릭 수집 중 오류: {}", e.getMessage());
        }
        
        return gcMetrics;
    }
    
    /**
     * Duration을 읽기 쉬운 형태로 포맷
     */
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        if (days > 0) {
            return String.format("%d일 %d시간 %d분", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
    
    // === Helper Methods ===
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}