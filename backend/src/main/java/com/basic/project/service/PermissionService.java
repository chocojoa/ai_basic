package com.basic.project.service;

import com.basic.project.domain.RoleMenu;
import com.basic.project.mapper.RoleMenuMapper;
import com.basic.project.mapper.RoleMapper;
import com.basic.project.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class PermissionService {

    private final RoleMenuMapper roleMenuMapper;

    private final RoleMapper roleMapper;

    private final MenuMapper menuMapper;

    public List<RoleMenu> getAllPermissions() {
        return roleMenuMapper.findAll();
    }

    public Optional<RoleMenu> getPermissionById(Long id) {
        return roleMenuMapper.findById(id);
    }

    public Optional<RoleMenu> getPermissionByRoleAndMenu(Long roleId, Long menuId) {
        return roleMenuMapper.findByRoleIdAndMenuId(roleId, menuId);
    }

    public List<RoleMenu> getPermissionsByRoleId(Long roleId) {
        return roleMenuMapper.findByRoleId(roleId);
    }

    public List<RoleMenu> getPermissionsByMenuId(Long menuId) {
        return roleMenuMapper.findByMenuId(menuId);
    }

    public List<RoleMenu> getPermissionsByUserId(Long userId) {
        return roleMenuMapper.findPermissionsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<RoleMenu> getPermissionsWithMenuDetailsByRoleId(Long roleId) {
        return roleMenuMapper.findByRoleIdWithMenuDetails(roleId);
    }

    public List<RoleMenu> getPermissionsWithRoleDetailsByMenuId(Long menuId) {
        return roleMenuMapper.findByMenuIdWithRoleDetails(menuId);
    }

    public RoleMenu createPermission(RoleMenu roleMenu) {
        if (roleMapper.findById(roleMenu.getRoleId()).isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        if (menuMapper.findById(roleMenu.getMenuId()).isEmpty()) {
            throw new NoSuchElementException("Menu not found");
        }
        if (roleMenuMapper.existsByRoleIdAndMenuId(roleMenu.getRoleId(), roleMenu.getMenuId())) {
            throw new IllegalArgumentException("Permission already exists for this role and menu");
        }

        if (roleMenu.getCanRead() == null) {
            roleMenu.setCanRead(true);
        }
        if (roleMenu.getCanWrite() == null) {
            roleMenu.setCanWrite(false);
        }
        if (roleMenu.getCanDelete() == null) {
            roleMenu.setCanDelete(false);
        }

        roleMenuMapper.insert(roleMenu);
        return roleMenu;
    }

    public RoleMenu updatePermission(Long id, RoleMenu roleMenu) {
        Optional<RoleMenu> existingPermission = roleMenuMapper.findById(id);
        if (existingPermission.isEmpty()) {
            throw new NoSuchElementException("Permission not found");
        }

        roleMenu.setId(id);
        roleMenuMapper.update(roleMenu);
        return roleMenuMapper.findById(id).orElse(null);
    }

    public void deletePermission(Long id) {
        Optional<RoleMenu> permission = roleMenuMapper.findById(id);
        if (permission.isEmpty()) {
            throw new NoSuchElementException("Permission not found");
        }
        roleMenuMapper.delete(id);
    }

    public void deletePermissionByRoleAndMenu(Long roleId, Long menuId) {
        if (!roleMenuMapper.existsByRoleIdAndMenuId(roleId, menuId)) {
            throw new NoSuchElementException("Permission not found");
        }
        roleMenuMapper.deleteByRoleIdAndMenuId(roleId, menuId);
    }

    public void deletePermissionsByRoleId(Long roleId) {
        roleMenuMapper.deleteByRoleId(roleId);
    }

    public void deletePermissionsByMenuId(Long menuId) {
        roleMenuMapper.deleteByMenuId(menuId);
    }

    public void batchCreatePermissions(List<RoleMenu> roleMenus) {
        for (RoleMenu roleMenu : roleMenus) {
            if (roleMapper.findById(roleMenu.getRoleId()).isEmpty()) {
                throw new NoSuchElementException("Role not found: " + roleMenu.getRoleId());
            }
            if (menuMapper.findById(roleMenu.getMenuId()).isEmpty()) {
                throw new NoSuchElementException("Menu not found: " + roleMenu.getMenuId());
            }
            if (roleMenuMapper.existsByRoleIdAndMenuId(roleMenu.getRoleId(), roleMenu.getMenuId())) {
                throw new IllegalArgumentException("Permission already exists for role " + roleMenu.getRoleId() + " and menu " + roleMenu.getMenuId());
            }

            if (roleMenu.getCanRead() == null) {
                roleMenu.setCanRead(true);
            }
            if (roleMenu.getCanWrite() == null) {
                roleMenu.setCanWrite(false);
            }
            if (roleMenu.getCanDelete() == null) {
                roleMenu.setCanDelete(false);
            }
        }
        roleMenuMapper.batchInsert(roleMenus);
    }

    public void batchUpdatePermissionsByRoleId(Long roleId, List<RoleMenu> roleMenus) {
        roleMenuMapper.batchDeleteByRoleId(roleId);
        
        for (RoleMenu roleMenu : roleMenus) {
            roleMenu.setRoleId(roleId);
            if (menuMapper.findById(roleMenu.getMenuId()).isEmpty()) {
                throw new NoSuchElementException("Menu not found: " + roleMenu.getMenuId());
            }
            if (roleMenu.getCanRead() == null) {
                roleMenu.setCanRead(true);
            }
            if (roleMenu.getCanWrite() == null) {
                roleMenu.setCanWrite(false);
            }
            if (roleMenu.getCanDelete() == null) {
                roleMenu.setCanDelete(false);
            }
        }
        
        if (!roleMenus.isEmpty()) {
            roleMenuMapper.batchInsert(roleMenus);
        }
    }

    public boolean hasPermission(Long userId, Long menuId, String permissionType) {
        return roleMenuMapper.hasPermission(userId, menuId, permissionType);
    }

    public boolean hasReadPermission(Long userId, Long menuId) {
        return roleMenuMapper.hasPermission(userId, menuId, "READ");
    }

    public boolean hasWritePermission(Long userId, Long menuId) {
        return roleMenuMapper.hasPermission(userId, menuId, "WRITE");
    }

    public boolean hasDeletePermission(Long userId, Long menuId) {
        return roleMenuMapper.hasPermission(userId, menuId, "DELETE");
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.findMenuIdsByRoleId(roleId);
    }

    public List<Long> getRoleIdsByMenuId(Long menuId) {
        return roleMenuMapper.findRoleIdsByMenuId(menuId);
    }

    public int getTotalPermissionCount() {
        return roleMenuMapper.count();
    }

    public List<RoleMenu> getPermissionsWithPagination(int page, int size) {
        int offset = (page - 1) * size;
        return roleMenuMapper.findWithPagination(offset, size);
    }

    public boolean existsByRoleIdAndMenuId(Long roleId, Long menuId) {
        return roleMenuMapper.existsByRoleIdAndMenuId(roleId, menuId);
    }
}