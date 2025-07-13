package com.basic.project.controller;

import com.basic.project.domain.RoleMenu;
import com.basic.project.dto.ApiResponse;
import com.basic.project.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getAllPermissions() {
        try {
            List<RoleMenu> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<RoleMenu>> getPermissionById(@PathVariable Long id) {
        try {
            Optional<RoleMenu> permission = permissionService.getPermissionById(id);
            if (permission.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(permission.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Permission not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permission: " + e.getMessage()));
        }
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsByRoleId(@PathVariable Long roleId) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions by role: " + e.getMessage()));
        }
    }

    @GetMapping("/role/{roleId}/details")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsWithMenuDetailsByRoleId(@PathVariable Long roleId) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsWithMenuDetailsByRoleId(roleId);
            if (permissions == null) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
            }
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅을 위한 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions with menu details: " + e.getMessage()));
        }
    }

    @GetMapping("/menu/{menuId}")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsByMenuId(@PathVariable Long menuId) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsByMenuId(menuId);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions by menu: " + e.getMessage()));
        }
    }

    @GetMapping("/menu/{menuId}/details")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsWithRoleDetailsByMenuId(@PathVariable Long menuId) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsWithRoleDetailsByMenuId(menuId);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions with role details: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsByUserId(@PathVariable Long userId) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions by user: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("@menuPermissionService.hasWritePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<RoleMenu>> createPermission(@RequestBody RoleMenu roleMenu) {
        try {
            RoleMenu createdPermission = permissionService.createPermission(roleMenu);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdPermission));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create permission: " + e.getMessage()));
        }
    }

    @PostMapping("/batch")
    @PreAuthorize("@menuPermissionService.hasWritePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> batchCreatePermissions(@RequestBody List<RoleMenu> roleMenus) {
        try {
            permissionService.batchCreatePermissions(roleMenus);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to batch create permissions: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasWritePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<RoleMenu>> updatePermission(@PathVariable Long id, @RequestBody RoleMenu roleMenu) {
        try {
            RoleMenu updatedPermission = permissionService.updatePermission(id, roleMenu);
            return ResponseEntity.ok(ApiResponse.success(updatedPermission));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update permission: " + e.getMessage()));
        }
    }

    @PutMapping("/role/{roleId}/batch")
    @PreAuthorize("@menuPermissionService.hasWritePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> batchUpdatePermissionsByRoleId(@PathVariable Long roleId, @RequestBody List<RoleMenu> roleMenus) {
        try {
            permissionService.batchUpdatePermissionsByRoleId(roleId, roleMenus);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to batch update permissions: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@menuPermissionService.hasDeletePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete permission: " + e.getMessage()));
        }
    }

    @DeleteMapping("/role/{roleId}/menu/{menuId}")
    @PreAuthorize("@menuPermissionService.hasDeletePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> deletePermissionByRoleAndMenu(@PathVariable Long roleId, @PathVariable Long menuId) {
        try {
            permissionService.deletePermissionByRoleAndMenu(roleId, menuId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete permission: " + e.getMessage()));
        }
    }

    @DeleteMapping("/role/{roleId}")
    @PreAuthorize("@menuPermissionService.hasDeletePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> deletePermissionsByRoleId(@PathVariable Long roleId) {
        try {
            permissionService.deletePermissionsByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete permissions by role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/menu/{menuId}")
    @PreAuthorize("@menuPermissionService.hasDeletePermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Void>> deletePermissionsByMenuId(@PathVariable Long menuId) {
        try {
            permissionService.deletePermissionsByMenuId(menuId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete permissions by menu: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(
            @RequestParam Long userId,
            @RequestParam Long menuId,
            @RequestParam String permissionType) {
        try {
            boolean hasPermission = permissionService.hasPermission(userId, menuId, permissionType);
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check permission: " + e.getMessage()));
        }
    }

    @GetMapping("/check/read")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Boolean>> checkReadPermission(
            @RequestParam Long userId,
            @RequestParam Long menuId) {
        try {
            boolean hasPermission = permissionService.hasReadPermission(userId, menuId);
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check read permission: " + e.getMessage()));
        }
    }

    @GetMapping("/check/write")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Boolean>> checkWritePermission(
            @RequestParam Long userId,
            @RequestParam Long menuId) {
        try {
            boolean hasPermission = permissionService.hasWritePermission(userId, menuId);
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check write permission: " + e.getMessage()));
        }
    }

    @GetMapping("/check/delete")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Boolean>> checkDeletePermission(
            @RequestParam Long userId,
            @RequestParam Long menuId) {
        try {
            boolean hasPermission = permissionService.hasDeletePermission(userId, menuId);
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check delete permission: " + e.getMessage()));
        }
    }

    @GetMapping("/role/{roleId}/menus")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Long>>> getMenuIdsByRoleId(@PathVariable Long roleId) {
        try {
            List<Long> menuIds = permissionService.getMenuIdsByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(menuIds));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch menu IDs by role: " + e.getMessage()));
        }
    }

    @GetMapping("/menu/{menuId}/roles")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<Long>>> getRoleIdsByMenuId(@PathVariable Long menuId) {
        try {
            List<Long> roleIds = permissionService.getRoleIdsByMenuId(menuId);
            return ResponseEntity.ok(ApiResponse.success(roleIds));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch role IDs by menu: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Integer>> getTotalPermissionCount() {
        try {
            int count = permissionService.getTotalPermissionCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get permission count: " + e.getMessage()));
        }
    }

    @GetMapping("/page")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<RoleMenu>>> getPermissionsWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<RoleMenu> permissions = permissionService.getPermissionsWithPagination(page, size);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch permissions with pagination: " + e.getMessage()));
        }
    }

    @GetMapping("/exists")
    @PreAuthorize("@menuPermissionService.hasReadPermission('PERMISSION_MANAGEMENT')")
    public ResponseEntity<ApiResponse<Boolean>> existsByRoleIdAndMenuId(
            @RequestParam Long roleId,
            @RequestParam Long menuId) {
        try {
            boolean exists = permissionService.existsByRoleIdAndMenuId(roleId, menuId);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check permission existence: " + e.getMessage()));
        }
    }
}