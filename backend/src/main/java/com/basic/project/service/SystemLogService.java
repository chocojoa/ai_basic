package com.basic.project.service;

import com.basic.project.domain.SystemLog;
import com.basic.project.dto.LogSearchRequest;
import com.basic.project.dto.LogStatsResponse;
import com.basic.project.mapper.SystemLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogService {
    
    private final SystemLogMapper systemLogMapper;
    private final Random random = new Random();
    
    private static final String LOG_LEVEL_WARNING = "WARNING";
    
    @Async
    @Transactional
    public void createLog(String level, String username, String action, String message, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level(level)
                .username(username)
                .action(action)
                .message(message)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void createLog(String level, String username, String action, String message, String ipAddress, String userAgent, String details) {
        SystemLog systemLog = SystemLog.builder()
                .level(level)
                .username(username)
                .action(action)
                .message(message)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    public List<SystemLog> getAllLogs() {
        return systemLogMapper.findAll();
    }
    
    public List<SystemLog> getLogsWithPagination(int page, int size) {
        int offset = page * size;
        return systemLogMapper.findWithPagination(offset, size);
    }
    
    public List<SystemLog> searchLogs(LogSearchRequest request) {
        // 페이징 처리를 위한 offset 계산
        int offset = request.getPage() * request.getSize();
        request.setPage(offset);
        
        return systemLogMapper.search(request);
    }
    
    public int getTotalCount() {
        return systemLogMapper.count();
    }
    
    public int getSearchCount(LogSearchRequest request) {
        return systemLogMapper.countBySearch(request);
    }
    
    public LogStatsResponse getLogStats() {
        return systemLogMapper.getStats();
    }
    
    public long getCountByLevel(String level) {
        return systemLogMapper.countByLevel(level);
    }
    
    @Transactional
    public void deleteOldLogs(int days) {
        systemLogMapper.deleteOldLogs(days);
        log.info("Deleted logs older than {} days", days);
    }
    
    // 편의 메서드들
    @Async
    @Transactional
    public void logInfo(String username, String action, String message, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level("INFO")
                .username(username)
                .action(action)
                .message(message)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logWarning(String username, String action, String message, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level(LOG_LEVEL_WARNING)
                .username(username)
                .action(action)
                .message(message)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logError(String username, String action, String message, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level("ERROR")
                .username(username)
                .action(action)
                .message(message)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logLogin(String username, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level("INFO")
                .username(username)
                .action("LOGIN")
                .message("사용자 로그인 성공")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logLogout(String username, String ipAddress, String userAgent) {
        SystemLog systemLog = SystemLog.builder()
                .level("INFO")
                .username(username)
                .action("LOGOUT")
                .message("사용자 로그아웃")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logLoginFailed(String username, String ipAddress, String userAgent, String reason) {
        SystemLog systemLog = SystemLog.builder()
                .level(LOG_LEVEL_WARNING)
                .username(username)
                .action("LOGIN_FAILED")
                .message("로그인 실패: " + reason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    @Async
    @Transactional
    public void logUnauthorizedAccess(String username, String ipAddress, String userAgent, String resource) {
        SystemLog systemLog = SystemLog.builder()
                .level(LOG_LEVEL_WARNING)
                .username(username)
                .action("UNAUTHORIZED_ACCESS")
                .message("권한 없는 접근 시도: " + resource)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        
        systemLogMapper.insert(systemLog);
    }
    
    /**
     * 개발 환경에서 사용할 현실적인 IP 주소 생성
     * 실제 운영 환경에서는 사용하지 않음
     */
    public String generateRealisticIpAddress(String originalIp) {
        // 로컬 IP인 경우 현실적인 IP로 변경
        if ("127.0.0.1".equals(originalIp) || "0:0:0:0:0:0:0:1".equals(originalIp) || "::1".equals(originalIp)) {
            String[] sampleIps = {
                "192.168.1." + (100 + random.nextInt(50)),  // 내부 네트워크
                "10.0.0." + (10 + random.nextInt(240)),     // 사설 IP
                "172.16.0." + (10 + random.nextInt(240)),   // 사설 IP
                "203.241.185." + (1 + random.nextInt(254)), // 공인 IP (예시)
                "211.106.114." + (1 + random.nextInt(254)), // 공인 IP (예시)
                "118.67.101." + (1 + random.nextInt(254)),  // 공인 IP (예시)
            };
            return sampleIps[random.nextInt(sampleIps.length)];
        }
        return originalIp;
    }
}