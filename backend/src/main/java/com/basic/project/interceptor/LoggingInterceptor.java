package com.basic.project.interceptor;

import com.basic.project.service.SystemLogService;
import com.basic.project.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {
    
    private final SystemLogService systemLogService;
    
    // API 경로 상수들
    private static final String API_AUTH_LOGIN = "/api/auth/login";
    private static final String API_AUTH_LOGOUT = "/api/auth/logout";
    private static final String API_USERS = "/api/users";
    private static final String API_ROLES = "/api/roles";
    private static final String API_MENUS = "/api/menus";
    private static final String API_PERMISSIONS = "/api/permissions";
    
    // HTTP 메소드 상수들
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인, 로그아웃 등 특정 API 호출 시 로그 생성
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "anonymous";
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            username = userPrincipal.getUsername();
        }
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 특정 API 호출에 대해서만 로그 생성
        if (shouldLog(uri, method)) {
            String action = getActionFromUri(uri, method);
            String message = getMessageFromUri(uri, method);
            
            // 개발 환경에서 현실적인 IP 주소 사용
            String finalIpAddress = systemLogService.generateRealisticIpAddress(ipAddress);
            
            systemLogService.logInfo(username, action, message, finalIpAddress, userAgent);
        }
        
        return true;
    }
    
    private static final Map<String, Set<String>> LOGGED_ENDPOINTS = Map.of(
        API_AUTH_LOGIN, Set.of(METHOD_POST),
        API_AUTH_LOGOUT, Set.of(METHOD_POST),
        API_USERS, Set.of(METHOD_POST, METHOD_PUT, METHOD_DELETE),
        API_ROLES, Set.of(METHOD_POST, METHOD_PUT, METHOD_DELETE),
        API_MENUS, Set.of(METHOD_POST, METHOD_PUT, METHOD_DELETE),
        API_PERMISSIONS, Set.of(METHOD_POST, METHOD_PUT)
    );
    
    private boolean shouldLog(String uri, String method) {
        return LOGGED_ENDPOINTS.entrySet().stream()
            .anyMatch(entry -> uri.contains(entry.getKey()) && entry.getValue().contains(method));
    }
    
    private static final Map<String, Map<String, String>> ACTION_MAPPINGS = Map.of(
        API_AUTH_LOGIN, Map.of(METHOD_POST, "LOGIN"),
        API_AUTH_LOGOUT, Map.of(METHOD_POST, "LOGOUT"),
        API_USERS, Map.of(METHOD_POST, "CREATE_USER", METHOD_PUT, "UPDATE_USER", METHOD_DELETE, "DELETE_USER"),
        API_ROLES, Map.of(METHOD_POST, "CREATE_ROLE", METHOD_PUT, "UPDATE_ROLE", METHOD_DELETE, "DELETE_ROLE"),
        API_MENUS, Map.of(METHOD_POST, "CREATE_MENU", METHOD_PUT, "UPDATE_MENU", METHOD_DELETE, "DELETE_MENU"),
        API_PERMISSIONS, Map.of(METHOD_POST, "UPDATE_PERMISSION", METHOD_PUT, "UPDATE_PERMISSION")
    );
    
    private String getActionFromUri(String uri, String method) {
        return ACTION_MAPPINGS.entrySet().stream()
            .filter(entry -> uri.contains(entry.getKey()))
            .map(entry -> entry.getValue().get(method))
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse("API_CALL");
    }
    
    private static final Map<String, Map<String, String>> MESSAGE_MAPPINGS = Map.of(
        API_AUTH_LOGIN, Map.of(METHOD_POST, "사용자 로그인 성공"),
        API_AUTH_LOGOUT, Map.of(METHOD_POST, "사용자 로그아웃"),
        API_USERS, Map.of(METHOD_POST, "새 사용자 생성", METHOD_PUT, "사용자 정보 수정", METHOD_DELETE, "사용자 삭제"),
        API_ROLES, Map.of(METHOD_POST, "새 역할 생성", METHOD_PUT, "역할 정보 수정", METHOD_DELETE, "역할 삭제"),
        API_MENUS, Map.of(METHOD_POST, "새 메뉴 생성", METHOD_PUT, "메뉴 정보 수정", METHOD_DELETE, "메뉴 삭제"),
        API_PERMISSIONS, Map.of(METHOD_POST, "권한 설정 수정", METHOD_PUT, "권한 설정 수정")
    );
    
    private String getMessageFromUri(String uri, String method) {
        return MESSAGE_MAPPINGS.entrySet().stream()
            .filter(entry -> uri.contains(entry.getKey()))
            .map(entry -> entry.getValue().get(method))
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse("API 호출: " + method + " " + uri);
    }
    
    private static final String[] PROXY_HEADERS = {
        "X-Forwarded-For", "X-Real-IP", "X-Original-Forwarded-For", "CF-Connecting-IP"
    };
    
    private String getClientIpAddress(HttpServletRequest request) {
        // 프록시 헤더들에서 IP 주소 찾기
        for (String header : PROXY_HEADERS) {
            String ip = extractIpFromHeader(request, header);
            if (ip != null) {
                return ip;
            }
        }
        
        // 기본 IP 주소 처리
        return processRemoteAddress(request.getRemoteAddr());
    }
    
    private String extractIpFromHeader(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (isValidIpHeader(headerValue)) {
            return "X-Forwarded-For".equals(headerName) || "X-Original-Forwarded-For".equals(headerName) 
                ? headerValue.split(",")[0].trim() 
                : headerValue;
        }
        return null;
    }
    
    private boolean isValidIpHeader(String headerValue) {
        return headerValue != null && !headerValue.isEmpty() && !"unknown".equalsIgnoreCase(headerValue);
    }
    
    private String processRemoteAddress(String remoteAddr) {
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            return "127.0.0.1";
        }
        
        // IPv6 루프백을 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        
        // IPv4 매핑된 IPv6 주소 처리
        if (remoteAddr.startsWith("::ffff:")) {
            return remoteAddr.substring(7);
        }
        
        return remoteAddr;
    }
}