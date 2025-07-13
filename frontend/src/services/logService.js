import api from './api';

const logService = {
  getLogs: async (page = 0, size = 20) => {
    return await api.get(`/logs?page=${page}&size=${size}`);
  },

  searchLogs: async (searchParams) => {
    return await api.post('/logs/search', searchParams);
  },

  getLogStats: async () => {
    return await api.get('/logs/stats');
  },

  getTotalCount: async () => {
    return await api.get('/logs/count');
  },

  getCountByLevel: async (level) => {
    return await api.get(`/logs/count/level/${level}`);
  },

  deleteOldLogs: async (days = 30) => {
    return await api.delete(`/logs/cleanup?days=${days}`);
  },

  createTestLog: async () => {
    return await api.post('/logs/test');
  }
};

export default logService;