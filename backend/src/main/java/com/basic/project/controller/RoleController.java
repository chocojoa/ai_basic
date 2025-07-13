package com.basic.project.controller;

import com.basic.project.domain.Role;
import com.basic.project.dto.ApiResponse;
import com.basic.project.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.NoSuchElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch roles: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        try {
            Optional<Role> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(role.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Role not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch role: " + e.getMessage()));
        }
    }

    @GetMapping("/name/{roleName}")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<Role>> getRoleByRoleName(@PathVariable String roleName) {
        try {
            Optional<Role> role = roleService.getRoleByRoleName(roleName);
            if (role.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(role.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Role not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch role: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Role>>> getActiveRoles() {
        try {
            List<Role> roles = roleService.getActiveRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch active roles: " + e.getMessage()));
        }
    }

    @GetMapping("/inactive")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Role>>> getInactiveRoles() {
        try {
            List<Role> roles = roleService.getInactiveRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch inactive roles: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Role>>> getRolesByUserId(@PathVariable Long userId) {
        try {
            List<Role> roles = roleService.getRolesByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch roles by user: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create role: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            Role updatedRole = roleService.updateRole(id, role);
            return ResponseEntity.ok(ApiResponse.success(updatedRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update role: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@menuPermissionService.canDeleteRoles()")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete role: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Void>> activateRole(@PathVariable Long id) {
        try {
            roleService.activateRole(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to activate role: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Void>> deactivateRole(@PathVariable Long id) {
        try {
            roleService.deactivateRole(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deactivate role: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<Integer>> getTotalRoleCount() {
        try {
            int count = roleService.getTotalRoleCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get role count: " + e.getMessage()));
        }
    }

    @GetMapping("/page")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Role>>> getRolesWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Role> roles = roleService.getRolesWithPagination(page, size);
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch roles with pagination: " + e.getMessage()));
        }
    }

    @PostMapping("/{roleId}/assign-user/{userId}")
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(@PathVariable Long roleId, @PathVariable Long userId) {
        try {
            roleService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to assign role to user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{roleId}/remove-user/{userId}")
    @PreAuthorize("@menuPermissionService.canManageRoles()")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(@PathVariable Long roleId, @PathVariable Long userId) {
        try {
            roleService.removeRoleFromUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove role from user: " + e.getMessage()));
        }
    }

    @GetMapping("/{roleId}/users")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<List<Long>>> getUserIdsByRoleId(@PathVariable Long roleId) {
        try {
            List<Long> userIds = roleService.getUserIdsByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(userIds));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch user IDs by role: " + e.getMessage()));
        }
    }

    @GetMapping("/{roleId}/exists")
    @PreAuthorize("@menuPermissionService.canReadRoles()")
    public ResponseEntity<ApiResponse<Boolean>> existsByRoleName(@PathVariable Long roleId, @RequestParam String roleName) {
        try {
            boolean exists = roleService.existsByRoleName(roleName);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check role existence: " + e.getMessage()));
        }
    }
}