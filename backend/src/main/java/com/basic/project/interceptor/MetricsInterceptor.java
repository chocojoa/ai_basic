package com.basic.project.interceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * API 메트릭 수집을 위한 인터셉터
 * Prometheus 메트릭을 자동으로 수집하여 모니터링 대시보드에서 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {
    
    private final MeterRegistry meterRegistry;
    
    // 메트릭 태그 상수
    private static final String TAG_METHOD = "method";
    private static final String TAG_ENDPOINT = "endpoint";
    private static final String TAG_STATUS = "status";
    
    // 요청 ID 속성 키
    private static final String REQUEST_ID_ATTRIBUTE = "metrics.request.id";
    private static final String REQUEST_TIMER_ATTRIBUTE = "metrics.request.timer";
    
    // 미리 생성된 메트릭 캐시
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // API 요청 시작 시간 기록
        String requestId = generateRequestId();
        Timer.Sample sample = Timer.start(meterRegistry);
        
        // request에 저장하여 후에 사용
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(REQUEST_TIMER_ATTRIBUTE, sample);
        
        // 요청 수 증가
        incrementRequestCounter(request);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // request에서 타이머 가져오기
        Timer.Sample sample = (Timer.Sample) request.getAttribute(REQUEST_TIMER_ATTRIBUTE);
        
        if (sample != null) {
            // 응답 시간 기록
            recordResponseTime(request, response, sample);
        }
        
        // 응답 상태별 카운터 증가
        incrementResponseCounter(request, response);
        
        // 에러 발생 시 에러 카운터 증가
        if (ex != null || response.getStatus() >= 400) {
            incrementErrorCounter(request, response, ex);
        }
        
        // 사용자별 API 사용량 기록
        recordUserApiUsage(request);
    }
    
    /**
     * 요청 수 카운터 증가
     */
    private void incrementRequestCounter(HttpServletRequest request) {
        String endpoint = getEndpointPattern(request);
        String method = request.getMethod();
        
        String counterKey = "api_requests_" + method + "_" + endpoint;
        Counter counter = counters.computeIfAbsent(counterKey, 
            k -> Counter.builder("api.requests.total")
                    .description("총 API 요청 수")
                    .tags(Tags.of(
                        TAG_METHOD, method,
                        TAG_ENDPOINT, endpoint
                    ))
                    .register(meterRegistry));
        
        counter.increment();
    }
    
    /**
     * 응답 시간 기록
     */
    private void recordResponseTime(HttpServletRequest request, HttpServletResponse response, Timer.Sample sample) {
        String endpoint = getEndpointPattern(request);
        String method = request.getMethod();
        String status = String.valueOf(response.getStatus());
        
        String timerKey = "api_response_time_" + method + "_" + endpoint + "_" + status;
        Timer timer = timers.computeIfAbsent(timerKey,
            k -> Timer.builder("api.response.time")
                    .description("API 응답 시간")
                    .tags(Tags.of(
                        TAG_METHOD, method,
                        TAG_ENDPOINT, endpoint,
                        TAG_STATUS, status
                    ))
                    .register(meterRegistry));
        
        sample.stop(timer);
    }
    
    /**
     * 응답 상태별 카운터 증가
     */
    private void incrementResponseCounter(HttpServletRequest request, HttpServletResponse response) {
        String endpoint = getEndpointPattern(request);
        String method = request.getMethod();
        String status = String.valueOf(response.getStatus());
        String statusClass = getStatusClass(response.getStatus());
        
        // 상태 코드별 카운터
        String counterKey = "api_responses_" + method + "_" + endpoint + "_" + status;
        Counter counter = counters.computeIfAbsent(counterKey,
            k -> Counter.builder("api.responses.total")
                    .description("API 응답 수 (상태 코드별)")
                    .tags(Tags.of(
                        TAG_METHOD, method,
                        TAG_ENDPOINT, endpoint,
                        TAG_STATUS, status,
                        "status_class", statusClass
                    ))
                    .register(meterRegistry));
        
        counter.increment();
    }
    
    /**
     * 에러 카운터 증가
     */
    private void incrementErrorCounter(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String endpoint = getEndpointPattern(request);
        String method = request.getMethod();
        String errorType = ex != null ? ex.getClass().getSimpleName() : "HTTP_ERROR";
        String status = String.valueOf(response.getStatus());
        
        String counterKey = "api_errors_" + method + "_" + endpoint + "_" + errorType;
        Counter counter = counters.computeIfAbsent(counterKey,
            k -> Counter.builder("api.errors.total")
                    .description("API 에러 수")
                    .tags(Tags.of(
                        TAG_METHOD, method,
                        TAG_ENDPOINT, endpoint,
                        "error_type", errorType,
                        TAG_STATUS, status
                    ))
                    .register(meterRegistry));
        
        counter.increment();
        
        // 에러 로그 기록
        if (ex != null) {
            log.error("API Error: {} {} - {}", method, request.getRequestURI(), ex.getMessage(), ex);
        } else if (response.getStatus() >= 400) {
            log.warn("API Error Response: {} {} - Status: {}", method, request.getRequestURI(), response.getStatus());
        }
    }
    
    /**
     * 사용자별 API 사용량 기록
     */
    private void recordUserApiUsage(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            String username = authentication.getName();
            String endpoint = getEndpointPattern(request);
            String method = request.getMethod();
            
            String counterKey = "user_api_usage_" + username + "_" + method + "_" + endpoint;
            Counter counter = counters.computeIfAbsent(counterKey,
                k -> Counter.builder("user.api.usage")
                        .description("사용자별 API 사용량")
                        .tags(Tags.of(
                            "username", username,
                            TAG_METHOD, method,
                            TAG_ENDPOINT, endpoint
                        ))
                        .register(meterRegistry));
            
            counter.increment();
        }
    }
    
    /**
     * 요청 ID 생성 (타이머 관리용)
     */
    private String generateRequestId() {
        return Thread.currentThread().getName() + "_" + System.nanoTime();
    }
    
    /**
     * 엔드포인트 패턴 추출 (동적 경로 매개변수 제거)
     */
    private String getEndpointPattern(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // 경로 매개변수를 패턴으로 변경
        return uri.replaceAll("/\\d+", "/{id}")
                 .replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{uuid}")
                 .replaceAll("/[a-zA-Z0-9_-]{20,}", "/{token}");
    }
    
    /**
     * HTTP 상태 코드 클래스 분류
     */
    private String getStatusClass(int status) {
        if (status < 200) return "1xx";
        if (status < 300) return "2xx";
        if (status < 400) return "3xx";
        if (status < 500) return "4xx";
        return "5xx";
    }
}