package com.basic.project.mapper;

import com.basic.project.domain.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMenuMapper {
    
    Optional<RoleMenu> findById(Long id);
    
    Optional<RoleMenu> findByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    List<RoleMenu> findByRoleId(Long roleId);
    
    List<RoleMenu> findByMenuId(Long menuId);
    
    List<RoleMenu> findAll();
    
    int insert(RoleMenu roleMenu);
    
    int update(RoleMenu roleMenu);
    
    int delete(Long id);
    
    int deleteByRoleId(Long roleId);
    
    int deleteByMenuId(Long menuId);
    
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    List<RoleMenu> findPermissionsByUserId(Long userId);
    
    boolean hasPermission(@Param("userId") Long userId, @Param("menuId") Long menuId, @Param("permissionType") String permissionType);
    
    List<Long> findMenuIdsByRoleId(Long roleId);
    
    List<Long> findRoleIdsByMenuId(Long menuId);
    
    int batchInsert(List<RoleMenu> roleMenus);
    
    int batchDeleteByRoleId(Long roleId);
    
    int count();
    
    List<RoleMenu> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    boolean existsByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    List<RoleMenu> findByRoleIdWithMenuDetails(Long roleId);
    
    List<RoleMenu> findByMenuIdWithRoleDetails(Long menuId);
}