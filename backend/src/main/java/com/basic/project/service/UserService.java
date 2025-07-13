package com.basic.project.service;

import com.basic.project.domain.User;
import com.basic.project.dto.RegisterRequest;
import com.basic.project.dto.UpdateProfileRequest;
import com.basic.project.dto.CreateUserRequest;
import com.basic.project.mapper.UserMapper;
import com.basic.project.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final RoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userMapper.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public List<User> getUsersByRoleId(Long roleId) {
        return userMapper.findByRoleId(roleId);
    }

    public List<User> getActiveUsers() {
        return userMapper.findByActive(true);
    }

    public List<User> getInactiveUsers() {
        return userMapper.findByActive(false);
    }

    public User createUser(User user) {
        if (userMapper.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        
        userMapper.insert(user);
        return user;
    }

    public User createUser(CreateUserRequest request) {
        validateUserCreationRequest(request);
        
        User user = buildUserFromRequest(request);
        userMapper.insert(user);
        
        assignRolesToUser(user.getId(), request.getRoleIds());
        
        return userMapper.findById(user.getId()).orElse(user);
    }

    private void validateUserCreationRequest(CreateUserRequest request) {
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private User buildUserFromRequest(CreateUserRequest request) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();
    }

    private void assignRolesToUser(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userMapper.insertUserRole(userId, roleId);
            }
        }
    }

    public User updateUser(Long id, User user) {
        Optional<User> existingUser = userMapper.findById(id);
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        Optional<User> userWithSameUsername = userMapper.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("Username already exists");
        }

        Optional<User> userWithSameEmail = userMapper.findByEmail(user.getEmail());
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setId(id);
        userMapper.update(user);
        return userMapper.findById(id).orElse(null);
    }

    public void deleteUser(Long id) {
        Optional<User> user = userMapper.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        userMapper.delete(id);
    }

    public void updatePassword(Long id, String newPassword) {
        Optional<User> user = userMapper.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(id, encodedPassword);
    }

    public void updateLastLogin(Long id) {
        userMapper.updateLastLogin(id);
    }

    public int getTotalUserCount() {
        return userMapper.count();
    }

    public List<User> getUsersWithPagination(int page, int size) {
        int offset = (page - 1) * size;
        return userMapper.findWithPagination(offset, size);
    }

    public void activateUser(Long id) {
        Optional<User> user = userMapper.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        User userEntity = user.get();
        userEntity.setIsActive(true);
        userMapper.update(userEntity);
    }

    public void deactivateUser(Long id) {
        Optional<User> user = userMapper.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        User userEntity = user.get();
        userEntity.setIsActive(false);
        userMapper.update(userEntity);
    }

    public User registerUser(RegisterRequest registerRequest) {
        if (!registerRequest.isPasswordConfirmed()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        if (userMapper.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 사용자명입니다");
        }

        if (userMapper.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .isActive(true)
                .build();

        userMapper.insert(user);

        var defaultRole = roleMapper.findByRoleName("USER");
        if (defaultRole.isPresent()) {
            roleMapper.assignRoleToUser(user.getId(), defaultRole.get().getId());
        }

        return user;
    }

    public User updateProfile(Long id, UpdateProfileRequest request) {
        Optional<User> existingUser = userMapper.findById(id);
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다");
        }

        User user = existingUser.get();
        
        // 이메일 중복 확인 (자신의 이메일이 아닌 경우)
        if (!user.getEmail().equals(request.getEmail())) {
            Optional<User> userWithSameEmail = userMapper.findByEmail(request.getEmail());
            if (userWithSameEmail.isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다");
            }
        }

        // 프로필 정보 업데이트
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        userMapper.update(user);
        return userMapper.findById(id).orElse(null);
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        Optional<User> existingUser = userMapper.findById(id);
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다");
        }

        User user = existingUser.get();
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 설정
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(id, encodedNewPassword);
    }
    
    /**
     * 관리자용 비밀번호 초기화
     * 기본 비밀번호로 초기화하고 강제 변경 플래그 설정
     * ADMIN 권한을 가진 사용자만 호출 가능
     */
    public void resetPassword(Long userId) {
        Optional<User> existingUser = userMapper.findById(userId);
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다");
        }
        
        User user = existingUser.get();
        
        // 기본 비밀번호로 초기화 (사용자명 + "123")
        String defaultPassword = user.getUsername() + "123";
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        userMapper.updatePassword(userId, encodedPassword);
        
        // 해당 사용자가 다음 로그인 시 비밀번호 변경을 강제하도록 플래그 설정
        userMapper.updatePasswordChangeRequired(userId, true);
    }
    
    /**
     * 사용자가 직접 비밀번호 변경 시 플래그 초기화
     */
    public void userChangePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> existingUser = userMapper.findById(userId);
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다");
        }
        
        User user = existingUser.get();
        
        // 강제 변경이 아닌 경우에만 현재 비밀번호 확인
        if (currentPassword != null && !currentPassword.isEmpty() && 
            !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }
        
        // 새 비밀번호 설정
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedNewPassword);
        
        // 비밀번호 변경 필수 플래그 초기화
        userMapper.updatePasswordChangeRequired(userId, false);
    }
}