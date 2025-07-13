package com.basic.project.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.basic.project.mapper.UserMapper;
import com.basic.project.mapper.SystemLogMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("java:S1118") // SonarCube: Utility classes should not have public constructors
public class ActuatorConfig {

    /**
     * 사용자 정의 애플리케이션 정보 제공
     */
    @Component
    @RequiredArgsConstructor
    public static class CustomInfoContributor implements InfoContributor {
        
        private final UserMapper userMapper;
        private final SystemLogMapper systemLogMapper;

        @Override
        public void contribute(Info.Builder builder) {
            try {
                // 사용자 통계
                int totalUsers = userMapper.countTotal();
                int activeUsers = userMapper.countByStatus("ACTIVE");
                
                // 시스템 로그 통계 (최근 24시간)
                LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
                int recentLogs = systemLogMapper.countByDateRange(oneDayAgo, LocalDateTime.now());
                
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("name", "웹 프로젝트 기본 틀");
                appInfo.put("description", "Spring Boot + React 기반 웹 애플리케이션");
                appInfo.put("version", "1.0.0");
                appInfo.put("buildTime", LocalDateTime.now().toString());
                appInfo.put("authType", "JWT Token Based");
                
                Map<String, Object> statistics = new HashMap<>();
                statistics.put("totalUsers", totalUsers);
                statistics.put("activeUsers", activeUsers);
                statistics.put("recentLogs", recentLogs);
                
                builder.withDetail("application", appInfo)
                       .withDetail("statistics", statistics);
                       
            } catch (Exception e) {
                builder.withDetail("error", "통계 정보 수집 중 오류: " + e.getMessage());
            }
        }
    }

    /**
     * 데이터베이스 연결 상태 확인
     */
    @Component
    @RequiredArgsConstructor
    public static class DatabaseHealthIndicator implements HealthIndicator {
        
        private final UserMapper userMapper;

        @Override
        public Health health() {
            try {
                // 간단한 쿼리 실행으로 DB 연결 확인
                userMapper.countTotal();
                
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "연결됨")
                        .withDetail("checkTime", LocalDateTime.now())
                        .build();
                        
            } catch (Exception e) {
                return Health.down()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "연결 실패")
                        .withDetail("error", e.getMessage())
                        .withDetail("checkTime", LocalDateTime.now())
                        .build();
            }
        }
    }

    /**
     * 애플리케이션 메트릭 설정
     */
    @Component
    @RequiredArgsConstructor
    public static class CustomMetrics {
        
        private final MeterRegistry meterRegistry;
        
        // 로그인 성공/실패 카운터
        private Counter loginSuccessCounter;
        private Counter loginFailureCounter;
        
        // API 응답 시간 타이머
        private Timer apiResponseTimer;
        
        @Bean
        public void initMetrics() {
            // 카운터 생성
            loginSuccessCounter = Counter.builder("auth.login.success")
                    .description("로그인 성공 횟수")
                    .register(meterRegistry);
                    
            loginFailureCounter = Counter.builder("auth.login.failure")
                    .description("로그인 실패 횟수")
                    .register(meterRegistry);
            
            // 타이머 생성
            apiResponseTimer = Timer.builder("api.response.time")
                    .description("API 응답 시간")
                    .register(meterRegistry);
        }
        
        // 메트릭 업데이트 메서드들
        public void incrementLoginSuccess() {
            if (loginSuccessCounter != null) {
                loginSuccessCounter.increment();
            }
        }
        
        public void incrementLoginFailure() {
            if (loginFailureCounter != null) {
                loginFailureCounter.increment();
            }
        }
        
        public Timer.Sample startApiTimer() {
            return apiResponseTimer != null ? Timer.start(meterRegistry) : null;
        }
    }

    /**
     * 시스템 리소스 모니터링
     */
    @Component
    public static class SystemResourceHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            Map<String, Object> details = new HashMap<>();
            details.put("maxMemory", formatBytes(maxMemory));
            details.put("totalMemory", formatBytes(totalMemory));
            details.put("usedMemory", formatBytes(usedMemory));
            details.put("freeMemory", formatBytes(freeMemory));
            details.put("memoryUsagePercent", String.format("%.2f%%", memoryUsagePercent));
            details.put("availableProcessors", runtime.availableProcessors());
            
            Health.Builder healthBuilder = memoryUsagePercent < 90 ? Health.up() : Health.down();
            
            return healthBuilder
                    .withDetails(details)
                    .build();
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}