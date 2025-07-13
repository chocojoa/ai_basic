package com.basic.project.config;

import com.basic.project.interceptor.LoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final LoggingInterceptor loggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/logs/**"); // 로그 API는 제외 (무한 루프 방지)
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스에 대한 캐싱 설정
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)));
        
        // API 문서 등 기타 리소스
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/docs/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
    }
}