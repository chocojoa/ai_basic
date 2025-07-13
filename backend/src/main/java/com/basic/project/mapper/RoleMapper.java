package com.basic.project.mapper;

import com.basic.project.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {
    
    Optional<Role> findById(Long id);
    
    Optional<Role> findByRoleName(String roleName);
    
    List<Role> findAll();
    
    List<Role> findByActive(Boolean isActive);
    
    List<Role> findByUserRole(Long userId);
    
    int insert(Role role);
    
    int update(Role role);
    
    int delete(Long id);
    
    int count();
    
    List<Role> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    int assignRoleToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    int removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    List<Role> findRolesByUserId(Long userId);
    
    List<Long> findUserIdsByRoleId(Long roleId);
    
    boolean existsByRoleName(String roleName);
    
    boolean isRoleAssignedToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);
}