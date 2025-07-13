package com.basic.project.service;

import com.basic.project.domain.Menu;
import com.basic.project.domain.RoleMenu;
import com.basic.project.domain.User;
import com.basic.project.mapper.MenuMapper;
import com.basic.project.mapper.RoleMenuMapper;
import com.basic.project.mapper.UserMapper;
import com.basic.project.enums.MenuCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuPermissionService {
    
    private final UserMapper userMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    
    private static final String MENU_MAPPING_NOT_FOUND_MESSAGE = "메뉴 매핑을 찾을 수 없습니다";
    private static final String LOG_FORMAT_MESSAGE_WITH_PATH = "{}: {}";
    
    // API 경로와 메뉴 코드 매핑 테이블
    private static final Map<String, String> API_MENU_MAPPING = new HashMap<>();
    static {
        // 대시보드
        API_MENU_MAPPING.put("/api/dashboard", MenuCode.DASHBOARD);
        
        // 사용자 관리
        API_MENU_MAPPING.put("/api/users", MenuCode.USER_MANAGEMENT);
        
        // 역할 관리
        API_MENU_MAPPING.put("/api/roles", MenuCode.ROLE_MANAGEMENT);
        
        // 메뉴 관리
        API_MENU_MAPPING.put("/api/menus", MenuCode.MENU_MANAGEMENT);
        
        // 권한 관리
        API_MENU_MAPPING.put("/api/permissions", MenuCode.PERMISSION_MANAGEMENT);
        API_MENU_MAPPING.put("/api/role-menus", MenuCode.PERMISSION_MANAGEMENT);
        
        // 로그 관리
        API_MENU_MAPPING.put("/api/logs", MenuCode.LOG_MANAGEMENT);
        
        // 시스템 모니터링
        API_MENU_MAPPING.put("/api/monitoring", MenuCode.SYSTEM_MONITORING);
        
        // 고급 검색
        API_MENU_MAPPING.put("/api/search", MenuCode.ADVANCED_SEARCH);
        
        // 내 정보 (프로필 관련)
        API_MENU_MAPPING.put("/api/auth/profile", MenuCode.MY_PROFILE);
        API_MENU_MAPPING.put("/api/auth/me", MenuCode.MY_PROFILE);
        API_MENU_MAPPING.put("/api/auth/password", MenuCode.MY_PROFILE);
        API_MENU_MAPPING.put("/api/auth/force-change-password", MenuCode.MY_PROFILE);
    }
    
    /**
     * 현재 사용자가 특정 메뉴에 대한 읽기 권한이 있는지 확인
     */
    public boolean hasReadPermission(String username, String menuCode) {
        return hasPermission(username, menuCode, "read");
    }
    
    /**
     * 현재 사용자가 특정 메뉴에 대한 쓰기 권한이 있는지 확인
     */
    public boolean hasWritePermission(String username, String menuCode) {
        return hasPermission(username, menuCode, "write");
    }
    
    /**
     * 현재 사용자가 특정 메뉴에 대한 삭제 권한이 있는지 확인
     */
    public boolean hasDeletePermission(String username, String menuCode) {
        return hasPermission(username, menuCode, "delete");
    }
    
    /**
     * API 경로에 기반하여 읽기 권한 확인
     */
    public boolean hasReadPermissionForApi(String username, String apiPath) {
        String menuCode = getMenuCodeByApiPath(apiPath);
        if (menuCode == null) {
            log.warn(LOG_FORMAT_MESSAGE_WITH_PATH, MENU_MAPPING_NOT_FOUND_MESSAGE, apiPath);
            return false;
        }
        return hasReadPermission(username, menuCode);
    }
    
    /**
     * API 경로에 기반하여 쓰기 권한 확인
     */
    public boolean hasWritePermissionForApi(String username, String apiPath) {
        String menuCode = getMenuCodeByApiPath(apiPath);
        if (menuCode == null) {
            log.warn(LOG_FORMAT_MESSAGE_WITH_PATH, MENU_MAPPING_NOT_FOUND_MESSAGE, apiPath);
            return false;
        }
        return hasWritePermission(username, menuCode);
    }
    
    /**
     * API 경로에 기반하여 삭제 권한 확인
     */
    public boolean hasDeletePermissionForApi(String username, String apiPath) {
        String menuCode = getMenuCodeByApiPath(apiPath);
        if (menuCode == null) {
            log.warn(LOG_FORMAT_MESSAGE_WITH_PATH, MENU_MAPPING_NOT_FOUND_MESSAGE, apiPath);
            return false;
        }
        return hasDeletePermission(username, menuCode);
    }
    
    /**
     * 현재 로그인한 사용자가 특정 메뉴에 대한 읽기 권한이 있는지 확인
     */
    public boolean hasReadPermission(String menuCode) {
        String username = getCurrentUsername();
        return username != null && hasReadPermission(username, menuCode);
    }
    
    /**
     * 현재 로그인한 사용자가 특정 메뉴에 대한 쓰기 권한이 있는지 확인
     */
    public boolean hasWritePermission(String menuCode) {
        String username = getCurrentUsername();
        return username != null && hasWritePermission(username, menuCode);
    }
    
    /**
     * 현재 로그인한 사용자가 특정 메뉴에 대한 삭제 권한이 있는지 확인
     */
    public boolean hasDeletePermission(String menuCode) {
        String username = getCurrentUsername();
        return username != null && hasDeletePermission(username, menuCode);
    }
    
    // ===== 간결한 헬퍼 메서드들 =====
    
    /**
     * 간결한 읽기 권한 확인 - @PreAuthorize에서 사용
     * 사용 예: @PreAuthorize("@menuPermissionService.canRead('USER_MANAGEMENT')")
     */
    public boolean canRead(String menuCode) {
        return hasReadPermission(menuCode);
    }
    
    /**
     * 간결한 쓰기 권한 확인 - @PreAuthorize에서 사용
     * 사용 예: @PreAuthorize("@menuPermissionService.canWrite('USER_MANAGEMENT')")
     */
    public boolean canWrite(String menuCode) {
        return hasWritePermission(menuCode);
    }
    
    /**
     * 간결한 삭제 권한 확인 - @PreAuthorize에서 사용
     * 사용 예: @PreAuthorize("@menuPermissionService.canDelete('USER_MANAGEMENT')")
     */
    public boolean canDelete(String menuCode) {
        return hasDeletePermission(menuCode);
    }
    
    /**
     * 메뉴에 대한 모든 권한 확인 (읽기, 쓰기, 삭제)
     * 사용 예: @PreAuthorize("@menuPermissionService.hasFullAccess('USER_MANAGEMENT')")
     */
    public boolean hasFullAccess(String menuCode) {
        return canRead(menuCode) && canWrite(menuCode) && canDelete(menuCode);
    }
    
    /**
     * 메뉴에 대한 관리 권한 확인 (읽기, 쓰기)
     * 사용 예: @PreAuthorize("@menuPermissionService.canManage('USER_MANAGEMENT')")
     */
    public boolean canManage(String menuCode) {
        return canRead(menuCode) && canWrite(menuCode);
    }
    
    /**
     * 메뉴에 대한 접근 권한만 확인 (읽기만)
     * 사용 예: @PreAuthorize("@menuPermissionService.canAccess('USER_MANAGEMENT')")
     */
    public boolean canAccess(String menuCode) {
        return canRead(menuCode);
    }
    
    // ===== MenuCode 상수 기반 헬퍼 메서드들 =====
    
    /**
     * 대시보드 읽기 권한 확인
     */
    public boolean canReadDashboard() {
        return canRead(MenuCode.DASHBOARD);
    }
    
    /**
     * 사용자 관리 권한 확인
     */
    public boolean canReadUsers() {
        return canRead(MenuCode.USER_MANAGEMENT);
    }
    
    public boolean canManageUsers() {
        return canManage(MenuCode.USER_MANAGEMENT);
    }
    
    public boolean canDeleteUsers() {
        return canDelete(MenuCode.USER_MANAGEMENT);
    }
    
    /**
     * 역할 관리 권한 확인
     */
    public boolean canReadRoles() {
        return canRead(MenuCode.ROLE_MANAGEMENT);
    }
    
    public boolean canManageRoles() {
        return canManage(MenuCode.ROLE_MANAGEMENT);
    }
    
    public boolean canDeleteRoles() {
        return canDelete(MenuCode.ROLE_MANAGEMENT);
    }
    
    /**
     * 메뉴 관리 권한 확인
     */
    public boolean canReadMenus() {
        return canRead(MenuCode.MENU_MANAGEMENT);
    }
    
    public boolean canManageMenus() {
        return canManage(MenuCode.MENU_MANAGEMENT);
    }
    
    public boolean canDeleteMenus() {
        return canDelete(MenuCode.MENU_MANAGEMENT);
    }
    
    /**
     * 권한 관리 권한 확인
     */
    public boolean canReadPermissions() {
        return canRead(MenuCode.PERMISSION_MANAGEMENT);
    }
    
    public boolean canManagePermissions() {
        return canManage(MenuCode.PERMISSION_MANAGEMENT);
    }
    
    /**
     * 로그 관리 권한 확인
     */
    public boolean canReadLogs() {
        return canRead(MenuCode.LOG_MANAGEMENT);
    }
    
    public boolean canManageLogs() {
        return canManage(MenuCode.LOG_MANAGEMENT);
    }
    
    public boolean canDeleteLogs() {
        return canDelete(MenuCode.LOG_MANAGEMENT);
    }
    
    /**
     * 내 정보 관리 권한 확인
     */
    public boolean canAccessProfile() {
        return canAccess(MenuCode.MY_PROFILE);
    }
    
    public boolean canManageProfile() {
        return canManage(MenuCode.MY_PROFILE);
    }
    
    /**
     * 시스템 모니터링 권한 확인
     */
    public boolean canReadSystemMonitoring() {
        return canRead(MenuCode.SYSTEM_MONITORING);
    }
    
    public boolean canManageSystemMonitoring() {
        return canManage(MenuCode.SYSTEM_MONITORING);
    }
    
    /**
     * 고급 검색 권한 확인
     */
    public boolean canReadAdvancedSearch() {
        return canRead(MenuCode.ADVANCED_SEARCH);
    }
    
    public boolean canManageAdvancedSearch() {
        return canManage(MenuCode.ADVANCED_SEARCH);
    }
    
    
    /**
     * 현재 로그인한 사용자의 사용자명 반환
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * 권한 확인 공통 메서드
     */
    private boolean hasPermission(String username, String menuCode, String permissionType) {
        try {
            log.debug("권한 확인 시작: username={}, menuCode={}, permissionType={}", username, menuCode, permissionType);
            
            var user = validateAndGetUser(username);
            if (user == null) return false;
            
            var userRoles = validateAndGetUserRoles(user.getId(), username);
            if (userRoles.isEmpty()) return false;
            
            var menu = findMenuByCodeOrName(menuCode);
            if (menu == null) return false;
            
            return checkRolePermissions(userRoles, menu, permissionType);
            
        } catch (Exception e) {
            log.error("권한 확인 중 오류 발생: username={}, menuCode={}, permissionType={}", 
                     username, menuCode, permissionType, e);
            return false;
        }
    }
    
    private User validateAndGetUser(String username) {
        var userOpt = userMapper.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("사용자를 찾을 수 없습니다: {}", username);
            return null;
        }
        var user = userOpt.get();
        log.debug("사용자 조회 성공: userId={}", user.getId());
        return user;
    }
    
    private List<String> validateAndGetUserRoles(Long userId, String username) {
        List<String> userRoles = userMapper.findRolesByUserId(userId);
        if (userRoles.isEmpty()) {
            log.warn("사용자 {}에게 할당된 역할이 없습니다", username);
            return Collections.emptyList();
        }
        log.debug("사용자 역할 조회 성공: roles={}", userRoles);
        return userRoles;
    }
    
    private Menu findMenuByCodeOrName(String menuCode) {
        var menu = menuMapper.findByMenuCode(menuCode);
        if (menu == null) {
            String menuName = getMenuNameFromCode(menuCode);
            menu = menuMapper.findByMenuName(menuName);
            if (menu == null) {
                log.warn("메뉴를 찾을 수 없습니다: code={}, name={}", menuCode, menuName);
                return null;
            }
            log.debug("메뉴명으로 조회 성공: menuName={}", menuName);
        }
        log.debug("메뉴 조회 성공: menuId={}, menuName={}", menu.getId(), menu.getMenuName());
        return menu;
    }
    
    private boolean checkRolePermissions(List<String> userRoles, Menu menu, String permissionType) {
        for (String roleName : userRoles) {
            var role = userMapper.findRoleByName(roleName);
            if (role != null) {
                log.debug("역할 조회 성공: roleId={}, roleName={}", role.getId(), role.getRoleName());
                var roleMenuOpt = roleMenuMapper.findByRoleIdAndMenuId(role.getId(), menu.getId());
                if (roleMenuOpt.isPresent()) {
                    var roleMenu = roleMenuOpt.get();
                    log.debug("역할-메뉴 권한 조회 성공: canRead={}, canWrite={}, canDelete={}", 
                            roleMenu.getCanRead(), roleMenu.getCanWrite(), roleMenu.getCanDelete());
                    if (evaluatePermissionType(roleMenu, permissionType)) {
                        return true;
                    }
                } else {
                    log.debug("역할-메뉴 권한 매핑이 없습니다: roleId={}, menuId={}", role.getId(), menu.getId());
                }
            } else {
                log.debug("역할을 찾을 수 없습니다: {}", roleName);
            }
        }
        
        log.debug("권한 확인 실패: 모든 역할에 대해 권한이 없습니다");
        return false;
    }
    
    private boolean evaluatePermissionType(RoleMenu roleMenu, String permissionType) {
        switch (permissionType) {
            case "read":
                if (Boolean.TRUE.equals(roleMenu.getCanRead())) {
                    log.debug("읽기 권한 허용");
                    return true;
                }
                break;
            case "write":
                if (Boolean.TRUE.equals(roleMenu.getCanWrite())) {
                    log.debug("쓰기 권한 허용");
                    return true;
                }
                break;
            case "delete":
                if (Boolean.TRUE.equals(roleMenu.getCanDelete())) {
                    log.debug("삭제 권한 허용");
                    return true;
                }
                break;
            default:
                log.warn("알 수 없는 권한 타입: {}", permissionType);
                return false;
        }
        return false;
    }
    
    /**
     * API 경로로부터 메뉴 코드 조회
     */
    private String getMenuCodeByApiPath(String apiPath) {
        // 정확한 매칭 먼저 확인
        String menuCode = API_MENU_MAPPING.get(apiPath);
        if (menuCode != null) {
            return menuCode;
        }
        
        // 패턴 매칭 (예: /api/users/1 -> /api/users)
        for (Map.Entry<String, String> entry : API_MENU_MAPPING.entrySet()) {
            if (apiPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 메뉴 코드를 메뉴명으로 변환 (임시 해결책)
     */
    private String getMenuNameFromCode(String menuCode) {
        switch (menuCode) {
            case "DASHBOARD":
                return "대시보드";
            case "USER_MANAGEMENT":
                return "회원 관리";
            case "ROLE_MANAGEMENT":
                return "역할 관리";
            case "MENU_MANAGEMENT":
                return "메뉴 관리";
            case "PERMISSION_MANAGEMENT":
                return "권한 관리";
            case "LOG_MANAGEMENT":
                return "로그 관리";
            case "MY_PROFILE":
                return "내 정보";
            case "SYSTEM_MANAGEMENT":
                return "시스템 관리";
            default:
                log.warn("알 수 없는 메뉴 코드: {}", menuCode);
                return menuCode;
        }
    }
    
    /**
     * 사용자가 접근 가능한 메뉴 목록 조회
     */
    public List<String> getAccessibleMenus(String username) {
        try {
            var userOpt = userMapper.findByUsername(username);
            if (userOpt.isEmpty()) {
                return List.of();
            }
            var user = userOpt.get();
            
            List<String> userRoles = userMapper.findRolesByUserId(user.getId());
            return menuMapper.findAccessibleMenusByRoles(userRoles);
            
        } catch (Exception e) {
            log.error("접근 가능한 메뉴 조회 중 오류 발생: username={}", username, e);
            return List.of();
        }
    }
}