import api from './api';

const permissionService = {
  getAllPermissions: async () => {
    return await api.get('/permissions');
  },

  getPermissionById: async (id) => {
    return await api.get(`/permissions/${id}`);
  },

  getPermissionsByRoleId: async (roleId) => {
    return await api.get(`/permissions/role/${roleId}`);
  },

  getPermissionsWithMenuDetailsByRoleId: async (roleId) => {
    return await api.get(`/permissions/role/${roleId}/details`);
  },

  getPermissionsByMenuId: async (menuId) => {
    return await api.get(`/permissions/menu/${menuId}`);
  },

  getPermissionsWithRoleDetailsByMenuId: async (menuId) => {
    return await api.get(`/permissions/menu/${menuId}/details`);
  },

  getPermissionsByUserId: async (userId) => {
    return await api.get(`/permissions/user/${userId}`);
  },

  createPermission: async (permissionData) => {
    return await api.post('/permissions', permissionData);
  },

  batchCreatePermissions: async (permissions) => {
    return await api.post('/permissions/batch', permissions);
  },

  updatePermission: async (id, permissionData) => {
    return await api.put(`/permissions/${id}`, permissionData);
  },

  batchUpdatePermissionsByRoleId: async (roleId, permissions) => {
    return await api.put(`/permissions/role/${roleId}/batch`, permissions);
  },

  deletePermission: async (id) => {
    return await api.delete(`/permissions/${id}`);
  },

  deletePermissionByRoleAndMenu: async (roleId, menuId) => {
    return await api.delete(`/permissions/role/${roleId}/menu/${menuId}`);
  },

  deletePermissionsByRoleId: async (roleId) => {
    return await api.delete(`/permissions/role/${roleId}`);
  },

  deletePermissionsByMenuId: async (menuId) => {
    return await api.delete(`/permissions/menu/${menuId}`);
  },

  checkPermission: async (userId, menuId, permissionType) => {
    return await api.get(`/permissions/check?userId=${userId}&menuId=${menuId}&permissionType=${permissionType}`);
  },

  checkReadPermission: async (userId, menuId) => {
    return await api.get(`/permissions/check/read?userId=${userId}&menuId=${menuId}`);
  },

  checkWritePermission: async (userId, menuId) => {
    return await api.get(`/permissions/check/write?userId=${userId}&menuId=${menuId}`);
  },

  checkDeletePermission: async (userId, menuId) => {
    return await api.get(`/permissions/check/delete?userId=${userId}&menuId=${menuId}`);
  },

  getMenuIdsByRoleId: async (roleId) => {
    return await api.get(`/permissions/role/${roleId}/menus`);
  },

  getRoleIdsByMenuId: async (menuId) => {
    return await api.get(`/permissions/menu/${menuId}/roles`);
  },

  getPermissionCount: async () => {
    return await api.get('/permissions/count');
  },

  getPermissionsWithPagination: async (page = 1, size = 10) => {
    return await api.get(`/permissions/page?page=${page}&size=${size}`);
  },

  checkPermissionExists: async (roleId, menuId) => {
    return await api.get(`/permissions/exists?roleId=${roleId}&menuId=${menuId}`);
  },
};

export default permissionService;