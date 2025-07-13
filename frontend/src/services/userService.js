import api from './api';

const userService = {
  getUsers: async (params = {}) => {
    return await api.get('/users', { params });
  },

  getUser: async (id) => {
    return await api.get(`/users/${id}`);
  },

  createUser: async (userData) => {
    return await api.post('/users', userData);
  },

  updateUser: async (id, userData) => {
    return await api.put(`/users/${id}`, userData);
  },

  deleteUser: async (id) => {
    return await api.delete(`/users/${id}`);
  },

  getUserRoles: async (id) => {
    return await api.get(`/users/${id}/roles`);
  },

  updateUserRoles: async (id, roleIds) => {
    return await api.put(`/users/${id}/roles`, { roleIds });
  },

  resetPassword: async (id, newPassword) => {
    return await api.put(`/users/${id}/password`, { newPassword });
  },

  adminResetPassword: async (id) => {
    const response = await api.put(`/users/${id}/reset-password`);
    return response;
  },

  toggleUserStatus: async (id) => {
    return await api.put(`/users/${id}/toggle-status`);
  },
};

export default userService;