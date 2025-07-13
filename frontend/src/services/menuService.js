import api from './api';

const menuService = {
  getMenus: async () => {
    return await api.get('/menus');
  },

  getMenuTree: async () => {
    return await api.get('/menus/tree');
  },

  getMenu: async (id) => {
    return await api.get(`/menus/${id}`);
  },

  getUserMenus: async () => {
    return await api.get('/menus/user');
  },

  getRootMenus: async () => {
    return await api.get('/menus/root');
  },

  getChildMenus: async (parentId) => {
    return await api.get(`/menus/children/${parentId}`);
  },

  createMenu: async (menuData) => {
    return await api.post('/menus', menuData);
  },

  updateMenu: async (id, menuData) => {
    return await api.put(`/menus/${id}`, menuData);
  },

  deleteMenu: async (id) => {
    return await api.delete(`/menus/${id}`);
  },

  updateMenuOrder: async (id, orderNum) => {
    return await api.put(`/menus/${id}/order?orderNum=${orderNum}`);
  },

  toggleMenuVisibility: async (id) => {
    return await api.put(`/menus/${id}/visibility`);
  },

  searchMenus: async (keyword) => {
    return await api.get(`/menus/search?keyword=${keyword}`);
  },

  getMenusWithPagination: async (page, size) => {
    return await api.get(`/menus/page?page=${page}&size=${size}`);
  },

  getTotalMenuCount: async () => {
    return await api.get('/menus/count');
  },
};

export default menuService;