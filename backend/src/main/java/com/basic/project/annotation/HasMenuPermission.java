package com.basic.project.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메뉴 권한 확인을 위한 커스텀 어노테이션
 * 사용 예:
 * - @HasMenuPermission.Read("USER_MANAGEMENT")
 * - @HasMenuPermission.Write("USER_MANAGEMENT") 
 * - @HasMenuPermission.Delete("USER_MANAGEMENT")
 * - @HasMenuPermission.Manage("USER_MANAGEMENT")
 * - @HasMenuPermission.Access("USER_MANAGEMENT")
 */
public class HasMenuPermission {
    
    /**
     * 읽기 권한 확인
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canRead(#menuCode)")
    public @interface Read {
        String value(); // 메뉴 코드
    }
    
    /**
     * 쓰기/관리 권한 확인 (읽기 + 쓰기)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManage(#menuCode)")
    public @interface Write {
        String value(); // 메뉴 코드
    }
    
    /**
     * 삭제 권한 확인
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canDelete(#menuCode)")
    public @interface Delete {
        String value(); // 메뉴 코드
    }
    
    /**
     * 관리 권한 확인 (읽기 + 쓰기)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canManage(#menuCode)")
    public @interface Manage {
        String value(); // 메뉴 코드
    }
    
    /**
     * 접근 권한 확인 (읽기만)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.canAccess(#menuCode)")
    public @interface Access {
        String value(); // 메뉴 코드
    }
    
    /**
     * 전체 권한 확인 (읽기 + 쓰기 + 삭제)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@menuPermissionService.hasFullAccess(#menuCode)")
    public @interface Full {
        String value(); // 메뉴 코드
    }
}