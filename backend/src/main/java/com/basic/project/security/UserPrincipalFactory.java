package com.basic.project.security;

import com.basic.project.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserPrincipal 객체 생성을 위한 팩토리 클래스
 * Builder 패턴을 사용하여 복잡한 UserPrincipal 객체를 안전하게 생성
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Accessors(fluent = true, chain = true)
public class UserPrincipalFactory {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean passwordChangeRequired;
    private Collection<? extends GrantedAuthority> authorities;
    
    /**
     * 새로운 UserPrincipalFactory 인스턴스 생성
     */
    public static UserPrincipalFactory create() {
        return new UserPrincipalFactory();
    }
    
    /**
     * User 엔티티로부터 UserPrincipal 객체를 생성하는 편의 메서드
     */
    public static UserPrincipal fromUser(User user) {
        List<GrantedAuthority> authorities = user.getRoles() != null 
            ? user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList())
            : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        return UserPrincipalFactory.create()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .authorities(authorities)
                .build();
    }
    
    /**
     * 설정된 값들로 UserPrincipal 객체를 생성
     */
    public UserPrincipal build() {
        return new UserPrincipal(id, username, email, password, fullName, phone, 
                                createdAt, lastLogin, passwordChangeRequired, authorities);
    }
}