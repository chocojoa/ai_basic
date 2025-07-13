package com.basic.project.controller;

import com.basic.project.domain.User;
import com.basic.project.dto.ApiResponse;
import com.basic.project.dto.CreateUserRequest;
import com.basic.project.service.UserService;
import com.basic.project.service.MenuPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.NoSuchElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "사용자 관리", description = "사용자 CRUD 및 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "전체 사용자 목록 조회", description = "시스템에 등록된 모든 사용자의 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "사용자 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "권한 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 오류"
        )
    })
    @GetMapping
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch users: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch user: " + e.getMessage()));
        }
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userService.getUserByUsername(username);
            if (user.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch user: " + e.getMessage()));
        }
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRoleId(@PathVariable Long roleId) {
        try {
            List<User> users = userService.getUsersByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch users by role: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<List<User>>> getActiveUsers() {
        try {
            List<User> users = userService.getActiveUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch active users: " + e.getMessage()));
        }
    }

    @GetMapping("/inactive")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<List<User>>> getInactiveUsers() {
        try {
            List<User> users = userService.getInactiveUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch inactive users: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("@menuPermissionService.canManageUsers()")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        try {
            User createdUser = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@menuPermissionService.canDeleteUsers()")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordData) {
        try {
            String newPassword = passwordData.get("password");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Password is required"));
            }
            userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update password: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("@menuPermissionService.canManageUsers()")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to activate user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("@menuPermissionService.canManageUsers()")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deactivate user: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<Integer>> getTotalUserCount() {
        try {
            int count = userService.getTotalUserCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get user count: " + e.getMessage()));
        }
    }

    @GetMapping("/page")
    @PreAuthorize("@menuPermissionService.canReadUsers()")
    public ResponseEntity<ApiResponse<List<User>>> getUsersWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<User> users = userService.getUsersWithPagination(page, size);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch users with pagination: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("@menuPermissionService.canManageUsers()")
    public ResponseEntity<ApiResponse<String>> resetPassword(@PathVariable Long id) {
        try {
            userService.resetPassword(id);
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 초기화되었습니다. 기본 비밀번호는 사용자명+123 입니다."));
            
        } catch (RuntimeException e) {
            log.error("비밀번호 초기화 실패 - 사용자 ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("404", e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 초기화 중 예외 발생 - 사용자 ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "비밀번호 초기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}