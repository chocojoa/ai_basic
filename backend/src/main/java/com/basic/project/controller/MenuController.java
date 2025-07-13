package com.basic.project.controller;

import com.basic.project.domain.Menu;
import com.basic.project.dto.ApiResponse;
import com.basic.project.service.MenuService;
import com.basic.project.service.MenuPermissionService;
import com.basic.project.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;
    
    @GetMapping
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        return ResponseEntity.ok(ApiResponse.success("메뉴 목록 조회 성공", menus));
    }
    
    @GetMapping("/tree")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTree() {
        List<Menu> menuTree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success("메뉴 트리 조회 성공", menuTree));
    }
    
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Menu>>> getUserMenus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = 
            (UserPrincipal) authentication.getPrincipal();
        
        log.info("사용자 메뉴 조회 요청 - 사용자 ID: {}", userPrincipal.getId());
        List<Menu> userMenus = menuService.getUserMenus(userPrincipal.getId());
        log.info("조회된 메뉴 수: {}", userMenus.size());
        userMenus.forEach(menu -> log.info("메뉴: {} (ID: {}, 부모ID: {})", 
            menu.getMenuName(), menu.getId(), menu.getParentId()));
        
        return ResponseEntity.ok(ApiResponse.success("사용자 메뉴 조회 성공", userMenus));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Menu>> getMenuById(@PathVariable Long id) {
        Optional<Menu> menu = menuService.getMenuById(id);
        if (menu.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("메뉴 조회 성공", menu.get()));
        } else {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("404", "메뉴를 찾을 수 없습니다"));
        }
    }
    
    @GetMapping("/root")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> getRootMenus() {
        List<Menu> rootMenus = menuService.getRootMenus();
        return ResponseEntity.ok(ApiResponse.success("루트 메뉴 조회 성공", rootMenus));
    }
    
    @GetMapping("/children/{parentId}")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> getChildMenus(@PathVariable Long parentId) {
        List<Menu> childMenus = menuService.getChildMenus(parentId);
        return ResponseEntity.ok(ApiResponse.success("하위 메뉴 조회 성공", childMenus));
    }
    
    @PostMapping
    @PreAuthorize("@menuPermissionService.hasWritePermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Menu>> createMenu(@Valid @RequestBody Menu menu) {
        try {
            Menu createdMenu = menuService.createMenu(menu);
            return ResponseEntity.ok(ApiResponse.success("메뉴 생성 성공", createdMenu));
        } catch (Exception e) {
            log.error("메뉴 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "메뉴 생성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasWritePermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Menu>> updateMenu(@PathVariable Long id, @Valid @RequestBody Menu menu) {
        try {
            menu.setId(id);
            Menu updatedMenu = menuService.updateMenu(menu);
            return ResponseEntity.ok(ApiResponse.success("메뉴 수정 성공", updatedMenu));
        } catch (Exception e) {
            log.error("메뉴 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "메뉴 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasDeletePermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<String>> deleteMenu(@PathVariable Long id) {
        try {
            menuService.deleteMenu(id);
            return ResponseEntity.ok(ApiResponse.success("메뉴 삭제 성공"));
        } catch (Exception e) {
            log.error("메뉴 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "메뉴 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/order")
    @PreAuthorize("@menuPermissionService.hasWritePermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<String>> updateMenuOrder(
            @PathVariable Long id, 
            @RequestParam Integer orderNum) {
        try {
            menuService.updateMenuOrder(id, orderNum);
            return ResponseEntity.ok(ApiResponse.success("메뉴 순서 변경 성공"));
        } catch (Exception e) {
            log.error("메뉴 순서 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "메뉴 순서 변경에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/visibility")
    @PreAuthorize("@menuPermissionService.hasWritePermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<String>> toggleMenuVisibility(@PathVariable Long id) {
        try {
            menuService.toggleMenuVisibility(id);
            return ResponseEntity.ok(ApiResponse.success("메뉴 표시 설정 변경 성공"));
        } catch (Exception e) {
            log.error("메뉴 표시 설정 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "메뉴 표시 설정 변경에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> searchMenus(@RequestParam String keyword) {
        List<Menu> menus = menuService.searchMenus(keyword);
        return ResponseEntity.ok(ApiResponse.success("메뉴 검색 성공", menus));
    }
    
    @GetMapping("/page")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenusWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Menu> menus = menuService.getMenusWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.success("메뉴 페이징 조회 성공", menus));
    }
    
    @GetMapping("/count")
    @PreAuthorize("@menuPermissionService.hasReadPermission('MENU_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Integer>> getTotalMenuCount() {
        int count = menuService.getTotalMenuCount();
        return ResponseEntity.ok(ApiResponse.success("메뉴 총 개수 조회 성공", count));
    }
}