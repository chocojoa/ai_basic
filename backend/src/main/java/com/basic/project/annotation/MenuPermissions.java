package com.basic.project.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 각 메뉴별 특화된 권한 어노테이션들
 * 사용하기 매우 간단하고 직관적
 */
public class MenuPermissions {
    
    // ===== 대시보드 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadDashboard()")
    public @interface ReadDashboard {}
    
    // ===== 사용자 관리 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public @interface ReadUsers {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManageUsers()")
    public @interface ManageUsers {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canDeleteUsers()")
    public @interface DeleteUsers {}
    
    // ===== 역할 관리 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public @interface ReadRoles {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public @interface ManageRoles {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canDeleteRoles()")
    public @interface DeleteRoles {}
    
    // ===== 메뉴 관리 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadMenus()")
    public @interface ReadMenus {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManageMenus()")
    public @interface ManageMenus {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canDeleteMenus()")
    public @interface DeleteMenus {}
    
    // ===== 권한 관리 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadPermissions()")
    public @interface ReadPermissions {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManagePermissions()")
    public @interface ManagePermissions {}
    
    // ===== 로그 관리 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canReadLogs()")
    public @interface ReadLogs {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManageLogs()")
    public @interface ManageLogs {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canDeleteLogs()")
    public @interface DeleteLogs {}
    
    // ===== 내 정보 =====
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canAccessProfile()")
    public @interface AccessProfile {}
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManageProfile()")
    public @interface ManageProfile {}
}