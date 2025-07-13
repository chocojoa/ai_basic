package com.basic.project.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        
        // 캐시 이름들 미리 설정
        cacheManager.setCacheNames(Arrays.asList(
            "users",
            "roles", 
            "menus",
            "permissions",
            "menuTree"
        ));
        
        return cacheManager;
    }
}