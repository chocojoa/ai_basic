package com.basic.project.service;

import com.basic.project.domain.Role;
import com.basic.project.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;

    public List<Role> getAllRoles() {
        return roleMapper.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleMapper.findById(id);
    }

    public Optional<Role> getRoleByRoleName(String roleName) {
        return roleMapper.findByRoleName(roleName);
    }

    public List<Role> getActiveRoles() {
        return roleMapper.findByActive(true);
    }

    public List<Role> getInactiveRoles() {
        return roleMapper.findByActive(false);
    }

    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.findRolesByUserId(userId);
    }

    public Role createRole(Role role) {
        if (roleMapper.existsByRoleName(role.getRoleName())) {
            throw new IllegalArgumentException("Role name already exists");
        }
        
        if (role.getIsActive() == null) {
            role.setIsActive(true);
        }
        
        roleMapper.insert(role);
        return role;
    }

    public Role updateRole(Long id, Role role) {
        Optional<Role> existingRole = roleMapper.findById(id);
        if (existingRole.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }

        Optional<Role> roleWithSameName = roleMapper.findByRoleName(role.getRoleName());
        if (roleWithSameName.isPresent() && !roleWithSameName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Role name already exists");
        }

        role.setId(id);
        roleMapper.update(role);
        return roleMapper.findById(id).orElse(null);
    }

    public void deleteRole(Long id) {
        Optional<Role> role = roleMapper.findById(id);
        if (role.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        
        List<Long> userIds = roleMapper.findUserIdsByRoleId(id);
        if (!userIds.isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }
        
        roleMapper.delete(id);
    }

    public int getTotalRoleCount() {
        return roleMapper.count();
    }

    public List<Role> getRolesWithPagination(int page, int size) {
        int offset = (page - 1) * size;
        return roleMapper.findWithPagination(offset, size);
    }

    public void assignRoleToUser(Long userId, Long roleId) {
        if (roleMapper.isRoleAssignedToUser(userId, roleId)) {
            throw new IllegalStateException("Role is already assigned to user");
        }
        
        Optional<Role> role = roleMapper.findById(roleId);
        if (role.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        
        roleMapper.assignRoleToUser(userId, roleId);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        if (!roleMapper.isRoleAssignedToUser(userId, roleId)) {
            throw new IllegalStateException("Role is not assigned to user");
        }
        
        roleMapper.removeRoleFromUser(userId, roleId);
    }

    public List<Long> getUserIdsByRoleId(Long roleId) {
        return roleMapper.findUserIdsByRoleId(roleId);
    }

    public boolean isRoleAssignedToUser(Long userId, Long roleId) {
        return roleMapper.isRoleAssignedToUser(userId, roleId);
    }

    public void activateRole(Long id) {
        Optional<Role> role = roleMapper.findById(id);
        if (role.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        Role roleEntity = role.get();
        roleEntity.setIsActive(true);
        roleMapper.update(roleEntity);
    }

    public void deactivateRole(Long id) {
        Optional<Role> role = roleMapper.findById(id);
        if (role.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        Role roleEntity = role.get();
        roleEntity.setIsActive(false);
        roleMapper.update(roleEntity);
    }

    public boolean existsByRoleName(String roleName) {
        return roleMapper.existsByRoleName(roleName);
    }
}