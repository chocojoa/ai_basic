import api from './api';

const authService = {
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    return response;
  },

  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    return response;
  },

  logout: async () => {
    const response = await api.post('/auth/logout');
    return response;
  },

  refreshToken: async (refreshToken) => {
    const response = await api.post('/auth/refresh', { refreshToken });
    return response;
  },

  getCurrentUser: async () => {    
    const response = await api.get('/auth/me');
    return response;
  },

  updateProfile: async (userData) => {
    const response = await api.put('/auth/profile', userData);
    return response;
  },

  changePassword: async (currentPassword, newPassword) => {
    const response = await api.put('/auth/password', {
      currentPassword,
      newPassword,
    });
    return response;
  },

  forceChangePassword: async (newPassword, confirmPassword) => {
    const response = await api.put('/auth/force-change-password', {
      newPassword,
      confirmPassword,
    });
    return response;
  },
};

export { authService };
export default authService;
