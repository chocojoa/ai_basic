import api from './api';

const roleService = {
  getRoles: async () => {
    return await api.get('/roles');
  },

  getRole: async (id) => {
    return await api.get(`/roles/${id}`);
  },

  createRole: async (roleData) => {
    return await api.post('/roles', roleData);
  },

  updateRole: async (id, roleData) => {
    return await api.put(`/roles/${id}`, roleData);
  },

  deleteRole: async (id) => {
    return await api.delete(`/roles/${id}`);
  },

  getActiveRoles: async () => {
    return await api.get('/roles/active');
  },

  getInactiveRoles: async () => {
    return await api.get('/roles/inactive');
  },

  getRolesByUserId: async (userId) => {
    return await api.get(`/roles/user/${userId}`);
  },

  activateRole: async (id) => {
    return await api.put(`/roles/${id}/activate`);
  },

  deactivateRole: async (id) => {
    return await api.put(`/roles/${id}/deactivate`);
  },

  assignRoleToUser: async (roleId, userId) => {
    return await api.post(`/roles/${roleId}/assign-user/${userId}`);
  },

  removeRoleFromUser: async (roleId, userId) => {
    return await api.delete(`/roles/${roleId}/remove-user/${userId}`);
  },

  getUserIdsByRoleId: async (roleId) => {
    return await api.get(`/roles/${roleId}/users`);
  },

  getRoleCount: async () => {
    return await api.get('/roles/count');
  },

  getRolesWithPagination: async (page = 1, size = 10) => {
    return await api.get(`/roles/page?page=${page}&size=${size}`);
  },

  checkRoleExists: async (roleName) => {
    return await api.get(`/roles/exists?roleName=${roleName}`);
  },
};

export default roleService;