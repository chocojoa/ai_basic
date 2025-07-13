package com.basic.project.mapper;

import com.basic.project.domain.User;
import com.basic.project.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface UserMapper {
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findAll();
    
    List<User> findByRoleId(Long roleId);
    
    List<User> findByActive(Boolean isActive);
    
    int insert(User user);
    
    int update(User user);
    
    int delete(Long id);
    
    int updateLastLogin(Long id);
    
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    int updatePasswordChangeRequired(@Param("id") Long id, @Param("required") Boolean required);
    
    int count();
    
    int countTotal();
    
    int countByStatus(@Param("status") String status);
    
    List<User> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    // 최적화된 메서드들
    List<User> findAllWithRoles();
    
    List<User> findWithPaginationAndRoles(@Param("offset") int offset, @Param("limit") int limit);
    
    List<Map<String, Object>> findRolesByUserIds(@Param("userIds") List<Long> userIds);
    
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    int deleteUserRoles(Long userId);
    
    List<String> findRolesByUserId(Long userId);
    
    Role findRoleByName(String roleName);
}