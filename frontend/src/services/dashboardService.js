import api from './api';

const dashboardService = {
  getStats: async () => {
    return await api.get('/dashboard/stats');
  },

  getRecentLogs: async (limit = 10) => {
    return await api.get(`/dashboard/recent-activities?limit=${limit}`);
  }
};

export default dashboardService;