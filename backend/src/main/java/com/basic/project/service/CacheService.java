package com.basic.project.service;

import com.basic.project.domain.Menu;
import com.basic.project.domain.Role;
import com.basic.project.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 캐시 관리 서비스
 * 자주 사용되는 데이터의 캐싱을 통한 성능 최적화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    
    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    
    /**
     * 사용자 정보 캐싱
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public User getCachedUser(Long id) {
        log.debug("캐시 미스 - 사용자 조회: {}", id);
        return userService.getUserById(id).orElse(null);
    }
    
    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    public User getCachedUserByUsername(String username) {
        log.debug("캐시 미스 - 사용자명으로 조회: {}", username);
        return userService.getUserByUsername(username).orElse(null);
    }
    
    @CachePut(value = "users", key = "#user.id")
    public User updateCachedUser(User user) {
        log.debug("사용자 캐시 업데이트: {}", user.getId());
        return user;
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void evictUser(Long id) {
        log.debug("사용자 캐시 삭제: {}", id);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void evictAllUsers() {
        log.debug("모든 사용자 캐시 삭제");
    }
    
    /**
     * 역할 정보 캐싱
     */
    @Cacheable(value = "roles", key = "'all'")
    public List<Role> getCachedRoles() {
        log.debug("캐시 미스 - 모든 역할 조회");
        return roleService.getAllRoles();
    }
    
    @Cacheable(value = "roles", key = "#id", unless = "#result == null")
    public Role getCachedRole(Long id) {
        log.debug("캐시 미스 - 역할 조회: {}", id);
        return roleService.getRoleById(id).orElse(null);
    }
    
    @CacheEvict(value = "roles", allEntries = true)
    public void evictAllRoles() {
        log.debug("모든 역할 캐시 삭제");
    }
    
    /**
     * 메뉴 정보 캐싱
     */
    @Cacheable(value = "menus", key = "'all'")
    public List<Menu> getCachedMenus() {
        log.debug("캐시 미스 - 모든 메뉴 조회");
        return menuService.getAllMenus();
    }
    
    @Cacheable(value = "menus", key = "'hierarchy'")
    public List<Menu> getCachedMenuHierarchy() {
        log.debug("캐시 미스 - 메뉴 계층 구조 조회");
        return menuService.getMenuTree();
    }
    
    @Cacheable(value = "user-menus", key = "#userId")
    public List<Menu> getCachedUserMenus(Long userId) {
        log.debug("캐시 미스 - 사용자 메뉴 조회: {}", userId);
        return menuService.getUserMenus(userId);
    }
    
    @CacheEvict(value = {"menus", "user-menus"}, allEntries = true)
    public void evictAllMenus() {
        log.debug("모든 메뉴 캐시 삭제");
    }
    
    @CacheEvict(value = "user-menus", key = "#userId")
    public void evictUserMenus(Long userId) {
        log.debug("사용자 메뉴 캐시 삭제: {}", userId);
    }
    
    /**
     * 권한 정보 캐싱
     */
    @Cacheable(value = "permissions", key = "'role:' + #roleId")
    public Map<String, Object> getCachedRolePermissions(Long roleId) {
        log.debug("캐시 미스 - 역할 권한 조회: {}", roleId);
        // 실제 권한 조회 로직은 PermissionService에서 구현
        return Map.of(); // 임시 반환
    }
    
    @CacheEvict(value = "permissions", allEntries = true)
    public void evictAllPermissions() {
        log.debug("모든 권한 캐시 삭제");
    }
    
    /**
     * 통계 정보 캐싱 (짧은 TTL 적용)
     */
    @Cacheable(value = "statistics", key = "'dashboard'")
    public Map<String, Object> getCachedDashboardStats() {
        log.debug("캐시 미스 - 대시보드 통계 조회");
        // 실제 통계 조회 로직은 DashboardService에서 구현
        return Map.of(); // 임시 반환
    }
    
    @CacheEvict(value = "statistics", allEntries = true)
    public void evictAllStatistics() {
        log.debug("모든 통계 캐시 삭제");
    }
    
    /**
     * 다중 캐시 무효화 (데이터 변경 시 관련 캐시 모두 삭제)
     */
    @Caching(evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "roles", allEntries = true),
        @CacheEvict(value = "menus", allEntries = true),
        @CacheEvict(value = "user-menus", allEntries = true),
        @CacheEvict(value = "permissions", allEntries = true),
        @CacheEvict(value = "statistics", allEntries = true)
    })
    public void evictAllCaches() {
        log.info("모든 캐시 무효화 실행");
    }
    
    /**
     * 사용자 관련 캐시 무효화
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#userId"),
        @CacheEvict(value = "user-menus", key = "#userId"),
        @CacheEvict(value = "statistics", allEntries = true)
    })
    public void evictUserRelatedCaches(Long userId) {
        log.debug("사용자 관련 캐시 무효화: {}", userId);
    }
    
    /**
     * 역할 관련 캐시 무효화
     */
    @Caching(evict = {
        @CacheEvict(value = "roles", allEntries = true),
        @CacheEvict(value = "permissions", allEntries = true),
        @CacheEvict(value = "user-menus", allEntries = true),
        @CacheEvict(value = "statistics", allEntries = true)
    })
    public void evictRoleRelatedCaches() {
        log.debug("역할 관련 캐시 무효화");
    }
    
    /**
     * 메뉴 관련 캐시 무효화
     */
    @Caching(evict = {
        @CacheEvict(value = "menus", allEntries = true),
        @CacheEvict(value = "user-menus", allEntries = true),
        @CacheEvict(value = "permissions", allEntries = true),
        @CacheEvict(value = "statistics", allEntries = true)
    })
    public void evictMenuRelatedCaches() {
        log.debug("메뉴 관련 캐시 무효화");
    }
}