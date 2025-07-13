package com.basic.project.config;

import com.basic.project.service.ApiMonitoringService;
import com.basic.project.interceptor.LoggingInterceptor;
import com.basic.project.interceptor.MetricsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final ApiMonitoringService apiMonitoringService;
    private final LoggingInterceptor loggingInterceptor;
    private final MetricsInterceptor metricsInterceptor;
    
    private static final String API_PATTERN = "/api/**";
    private static final String[] COMMON_EXCLUDE_PATTERNS = {
            "/api-docs/**",
            "/swagger-ui/**",
            "/actuator/**"
    };
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 메트릭 수집 인터셉터 (가장 먼저 등록)
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns(API_PATTERN)
                .excludePathPatterns(COMMON_EXCLUDE_PATTERNS);
        
        // 로깅 인터셉터
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(API_PATTERN)
                .excludePathPatterns(COMMON_EXCLUDE_PATTERNS);
        
        // API 모니터링 인터셉터
        String[] monitoringExcludes = {
                "/api/auth/login",
                "/api/auth/register",
                "/api/public/**",
                "/api-docs/**",
                "/swagger-ui/**",
                "/actuator/**"
        };
        registry.addInterceptor(apiMonitoringService)
                .addPathPatterns(API_PATTERN)
                .excludePathPatterns(monitoringExcludes);
    }
}