package com.basic.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiMonitoringService implements HandlerInterceptor {

    // 메트릭 키 상수들
    private static final String METRIC_TOTAL_REQUESTS = "totalRequests";
    private static final String METRIC_ERROR_RATE = "errorRate";
    
    // API 호출 통계 저장
    private final ConcurrentMap<String, ApiStatistics> apiStats = new ConcurrentHashMap<>();
    
    // 전체 API 통계
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicInteger currentActiveRequests = new AtomicInteger(0);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 활성 요청 수 증가
        currentActiveRequests.incrementAndGet();
        totalRequests.incrementAndGet();
        
        // API 경로 추출
        String apiPath = extractApiPath(request);
        String method = request.getMethod();
        String key = method + " " + apiPath;
        
        // API별 통계 업데이트
        apiStats.computeIfAbsent(key, k -> new ApiStatistics()).incrementRequests();
        
        log.debug("API 요청 시작 - {} {}", method, request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) throws Exception {
        // 후처리 로직 (필요시 구현)
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        try {
            long startTime = (Long) request.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            // 활성 요청 수 감소
            currentActiveRequests.decrementAndGet();
            
            String apiPath = extractApiPath(request);
            String method = request.getMethod();
            String key = method + " " + apiPath;
            int statusCode = response.getStatus();
            
            // API별 통계 업데이트
            ApiStatistics stats = apiStats.get(key);
            if (stats != null) {
                stats.addResponseTime(responseTime);
                if (statusCode >= 400) {
                    stats.incrementErrors();
                    totalErrors.incrementAndGet();
                }
                if (statusCode >= 200 && statusCode < 300) {
                    stats.incrementSuccesses();
                }
            }
            
            // 느린 API 로깅 (3초 이상)
            if (responseTime > 3000) {
                log.warn("느린 API 감지 - {} {} ({}ms)", method, request.getRequestURI(), responseTime);
            }
            
            // 에러 로깅
            if (statusCode >= 500) {
                log.error("서버 에러 발생 - {} {} (상태: {}, 시간: {}ms)", 
                        method, request.getRequestURI(), statusCode, responseTime);
            } else if (statusCode >= 400) {
                log.warn("클라이언트 에러 발생 - {} {} (상태: {}, 시간: {}ms)", 
                        method, request.getRequestURI(), statusCode, responseTime);
            }
            
            log.debug("API 요청 완료 - {} {} (상태: {}, 시간: {}ms)", 
                    method, request.getRequestURI(), statusCode, responseTime);
                    
        } catch (Exception e) {
            log.error("API 모니터링 후처리 중 오류 발생", e);
        }
    }
    
    /**
     * API 통계 정보 조회
     */
    public Map<String, Object> getApiStatistics() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        
        // 전체 통계
        Map<String, Object> overall = new ConcurrentHashMap<>();
        overall.put(METRIC_TOTAL_REQUESTS, totalRequests.get());
        overall.put("totalErrors", totalErrors.get());
        overall.put("currentActiveRequests", currentActiveRequests.get());
        overall.put(METRIC_ERROR_RATE, calculateErrorRate());
        
        result.put("overall", overall);
        
        // API별 상세 통계
        Map<String, Object> apiDetails = new ConcurrentHashMap<>();
        for (Map.Entry<String, ApiStatistics> entry : apiStats.entrySet()) {
            ApiStatistics stats = entry.getValue();
            Map<String, Object> detail = new ConcurrentHashMap<>();
            detail.put(METRIC_TOTAL_REQUESTS, stats.getTotalRequests());
            detail.put("successCount", stats.getSuccessCount());
            detail.put("errorCount", stats.getErrorCount());
            detail.put("averageResponseTime", stats.getAverageResponseTime());
            detail.put("maxResponseTime", stats.getMaxResponseTime());
            detail.put("minResponseTime", stats.getMinResponseTime());
            detail.put(METRIC_ERROR_RATE, stats.getErrorRate());
            
            apiDetails.put(entry.getKey(), detail);
        }
        result.put("apis", apiDetails);
        
        return result;
    }
    
    /**
     * 통계 초기화
     */
    public void resetStatistics() {
        apiStats.clear();
        totalRequests.set(0);
        totalErrors.set(0);
        // currentActiveRequests는 초기화하지 않음 (실시간 값)
        
        log.info("API 통계가 초기화되었습니다");
    }
    
    /**
     * 상위 N개 느린 API 조회
     */
    public Map<String, Object> getTopSlowApis(int limit) {
        return apiStats.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAverageResponseTime(), 
                                                  e1.getValue().getAverageResponseTime()))
                .limit(limit)
                .collect(ConcurrentHashMap::new,
                        (map, entry) -> {
                            Map<String, Object> detail = new ConcurrentHashMap<>();
                            ApiStatistics stats = entry.getValue();
                            detail.put("averageResponseTime", stats.getAverageResponseTime());
                            detail.put("maxResponseTime", stats.getMaxResponseTime());
                            detail.put(METRIC_TOTAL_REQUESTS, stats.getTotalRequests());
                            map.put(entry.getKey(), detail);
                        },
                        ConcurrentHashMap::putAll);
    }
    
    /**
     * 상위 N개 에러 API 조회
     */
    public Map<String, Object> getTopErrorApis(int limit) {
        return apiStats.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getErrorRate(), 
                                                  e1.getValue().getErrorRate()))
                .limit(limit)
                .collect(ConcurrentHashMap::new,
                        (map, entry) -> {
                            Map<String, Object> detail = new ConcurrentHashMap<>();
                            ApiStatistics stats = entry.getValue();
                            detail.put(METRIC_ERROR_RATE, stats.getErrorRate());
                            detail.put("errorCount", stats.getErrorCount());
                            detail.put(METRIC_TOTAL_REQUESTS, stats.getTotalRequests());
                            map.put(entry.getKey(), detail);
                        },
                        ConcurrentHashMap::putAll);
    }
    
    // === Private Helper Methods ===
    
    private String extractApiPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        if (contextPath != null && !contextPath.isEmpty()) {
            uri = uri.substring(contextPath.length());
        }
        
        // 경로 파라미터 제거 (예: /api/users/123 -> /api/users/{id})
        return normalizeApiPath(uri);
    }
    
    private String normalizeApiPath(String path) {
        // 숫자 ID를 {id}로 치환
        path = path.replaceAll("/\\d+", "/{id}");
        
        // UUID 패턴을 {uuid}로 치환
        path = path.replaceAll("/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", "/{uuid}");
        
        return path;
    }
    
    private double calculateErrorRate() {
        long total = totalRequests.get();
        if (total == 0) return 0.0;
        return (double) totalErrors.get() / total * 100;
    }
    
    // === Inner Classes ===
    
    private static class ApiStatistics {
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private volatile long maxResponseTime = 0;
        private volatile long minResponseTime = Long.MAX_VALUE;
        
        public void incrementRequests() {
            totalRequests.incrementAndGet();
        }
        
        public void incrementSuccesses() {
            successCount.incrementAndGet();
        }
        
        public void incrementErrors() {
            errorCount.incrementAndGet();
        }
        
        public void addResponseTime(long responseTime) {
            totalResponseTime.addAndGet(responseTime);
            updateMaxResponseTime(responseTime);
            updateMinResponseTime(responseTime);
        }
        
        private synchronized void updateMaxResponseTime(long responseTime) {
            if (responseTime > maxResponseTime) {
                maxResponseTime = responseTime;
            }
        }
        
        private synchronized void updateMinResponseTime(long responseTime) {
            if (responseTime < minResponseTime) {
                minResponseTime = responseTime;
            }
        }
        
        public long getTotalRequests() { return totalRequests.get(); }
        public long getSuccessCount() { return successCount.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getMaxResponseTime() { return maxResponseTime; }
        public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
        
        public double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0.0;
        }
        
        public double getErrorRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) errorCount.get() / total * 100 : 0.0;
        }
    }
}