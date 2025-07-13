package com.basic.project.controller;

import com.basic.project.dto.ApiResponse;
import com.basic.project.dto.ChangePasswordRequest;
import com.basic.project.dto.ForceChangePasswordRequest;
import com.basic.project.dto.LoginRequest;
import com.basic.project.dto.LoginResponse;
import com.basic.project.dto.RefreshTokenRequest;
import com.basic.project.dto.RegisterRequest;
import com.basic.project.dto.UpdateProfileRequest;
import com.basic.project.domain.User;
import com.basic.project.security.JwtTokenProvider;
import com.basic.project.security.UserPrincipal;
import com.basic.project.security.UserDetailsServiceImpl;
import com.basic.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    private static final String UNAUTHORIZED_USER_MESSAGE = "인증되지 않은 사용자입니다";
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            log.info("Authentication successful for username: {}", loginRequest.getUsername());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            
            UserPrincipal userPrincipal = 
                (UserPrincipal) authentication.getPrincipal();
            
            // Update last login timestamp
            userService.updateLastLogin(userPrincipal.getId());
            
            List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .lastLogin(userPrincipal.getLastLogin())
                .passwordChangeRequired(userPrincipal.getPasswordChangeRequired())
                .roles(roles)
                .build();
            
            LoginResponse loginResponse = LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .type("Bearer")
                .user(userInfo)
                .expiresIn(tokenProvider.getExpirationTime())
                .build();
            
            log.info("Login successful for username: {}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));
            
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for username: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", "잘못된 사용자명 또는 비밀번호입니다"));
        } catch (Exception e) {
            log.error("Login error for username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "로그인 중 오류가 발생했습니다"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());
        
        try {
            User user = userService.registerUser(registerRequest);
            user.setPassword(null); // 비밀번호 제거
            
            log.info("Registration successful for username: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다", user));
                    
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for username: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("400", e.getMessage()));
        } catch (NoSuchElementException e) {
            log.warn("Registration failed for username: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("404", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error for username: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "회원가입 중 오류가 발생했습니다"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("로그아웃 되었습니다"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        try {
            String refreshToken = request.getRefreshToken();
            
            if (!tokenProvider.validateToken(refreshToken)) {
                log.warn("Invalid refresh token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("400", "유효하지 않은 리프레시 토큰입니다"));
            }
            
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            UserPrincipal userPrincipal = 
                (UserPrincipal) userDetailsService.loadUserByUsername(username);
            
            // 새로운 토큰 생성
            String newAccessToken = tokenProvider.generateToken(userPrincipal);
            String newRefreshToken = tokenProvider.generateRefreshToken(userPrincipal);
            
            List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
            
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .lastLogin(userPrincipal.getLastLogin())
                .passwordChangeRequired(userPrincipal.getPasswordChangeRequired())
                .roles(roles)
                .build();
            
            LoginResponse loginResponse = LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .user(userInfo)
                .expiresIn(tokenProvider.getExpirationTime())
                .build();
            
            log.info("Token refresh successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", loginResponse));
            
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "토큰 갱신 중 오류가 발생했습니다"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            
            // 데이터베이스에서 최신 사용자 정보를 가져와서 반환
            try {
                UserPrincipal updatedUserPrincipal = 
                    (UserPrincipal) userDetailsService.loadUserByUsername(userPrincipal.getUsername());
                
                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(updatedUserPrincipal));
            } catch (Exception e) {
                log.error("Failed to load updated user info for: {}", userPrincipal.getUsername(), e);
                // 실패 시 기존 정보 반환
                return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(userPrincipal));
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("401", UNAUTHORIZED_USER_MESSAGE));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("401", UNAUTHORIZED_USER_MESSAGE));
        }
        
        UserPrincipal userPrincipal = 
            (UserPrincipal) authentication.getPrincipal();
        
        try {
            User updatedUser = userService.updateProfile(userPrincipal.getId(), request);
            updatedUser.setPassword(null); // 비밀번호 제거
            
            log.info("Profile updated successfully for user: {}", userPrincipal.getUsername());
            return ResponseEntity.ok(ApiResponse.success("프로필이 업데이트되었습니다", updatedUser));
            
        } catch (Exception e) {
            log.error("Profile update error for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "프로필 업데이트 중 오류가 발생했습니다"));
        }
    }
    
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("401", UNAUTHORIZED_USER_MESSAGE));
        }
        
        UserPrincipal userPrincipal = 
            (UserPrincipal) authentication.getPrincipal();
        
        try {
            userService.changePassword(userPrincipal.getId(), request.getCurrentPassword(), request.getNewPassword());
            
            log.info("Password changed successfully for user: {}", userPrincipal.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("비밀번호가 변경되었습니다"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Password change failed for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("400", e.getMessage()));
        } catch (NoSuchElementException e) {
            log.warn("Password change failed for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("404", e.getMessage()));
        } catch (Exception e) {
            log.error("Password change error for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "비밀번호 변경 중 오류가 발생했습니다"));
        }
    }
    
    @PutMapping("/force-change-password")
    public ResponseEntity<ApiResponse<String>> forceChangePassword(@Valid @RequestBody ForceChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("401", UNAUTHORIZED_USER_MESSAGE));
        }
        
        UserPrincipal userPrincipal = 
            (UserPrincipal) authentication.getPrincipal();
        
        try {
            // 비밀번호 확인 검증
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("400", "새 비밀번호가 일치하지 않습니다"));
            }
            
            // 강제 비밀번호 변경 (현재 비밀번호 검증 없음, 플래그 초기화 포함)
            userService.userChangePassword(userPrincipal.getId(), "", request.getNewPassword());
            
            log.info("Force password change successful for user: {}", userPrincipal.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("비밀번호가 변경되었습니다"));
            
        } catch (Exception e) {
            log.error("Force password change error for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("500", "비밀번호 변경 중 오류가 발생했습니다"));
        }
    }
    
}