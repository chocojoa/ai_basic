package com.basic.project.security;

import com.basic.project.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phone;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLogin;
    private final Boolean passwordChangeRequired;
    private final Collection<? extends GrantedAuthority> authorities;
    
    /**
     * User 엔티티로부터 UserPrincipal 생성
     */
    public static UserPrincipal create(User user) {
        return UserPrincipalFactory.fromUser(user);
    }
    
    // UserDetails interface methods - Lombok @Getter로 자동 생성되는 메서드들과 다름
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
}