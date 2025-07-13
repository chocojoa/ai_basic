import axios from "axios";
import store from "../store/store";
import {
  clearCredentials,
  refreshToken as refreshTokenAction,
} from "../store/slices/authSlice";

const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 토큰 갱신 중인지 추적
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const state = store.getState();
    const token = state.auth.token || localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // 401 에러이고 토큰 갱신 엔드포인트가 아닌 경우
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.includes("/auth/refresh")
    ) {
      if (isRefreshing) {
        // 이미 토큰 갱신 중이면 대기
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Redux action을 통한 토큰 갱신
        const result = await store.dispatch(refreshTokenAction()).unwrap();
        const newToken = result.token;

        if (newToken) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          processQueue(null, newToken);
          return api(originalRequest);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        store.dispatch(clearCredentials());

        // 로그인 페이지로 리다이렉트
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// 인증 API
export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (profile) => api.put('/auth/profile', profile),
  changePassword: (passwordData) => api.put('/auth/change-password', passwordData),
};

// 사용자 관리 API
export const userApi = {
  getUsers: (params = {}) => api.get('/users', { params }),
  getUserById: (id) => api.get(`/users/${id}`),
  createUser: (user) => api.post('/users', user),
  updateUser: (id, user) => api.put(`/users/${id}`, user),
  deleteUser: (id) => api.delete(`/users/${id}`),
  assignRoles: (userId, roleIds) => api.post(`/users/${userId}/roles`, { roleIds }),
  getUserStats: () => api.get('/users/stats'),
};

// 역할 관리 API
export const roleApi = {
  getRoles: (params = {}) => api.get('/roles', { params }),
  getRoleById: (id) => api.get(`/roles/${id}`),
  createRole: (role) => api.post('/roles', role),
  updateRole: (id, role) => api.put(`/roles/${id}`, role),
  deleteRole: (id) => api.delete(`/roles/${id}`),
  getRoleStats: () => api.get('/roles/stats'),
};

// 메뉴 관리 API
export const menuApi = {
  getMenus: () => api.get('/menus'),
  getMenuTree: () => api.get('/menus/tree'),
  getUserMenus: () => api.get('/menus/user'),
  getMenuById: (id) => api.get(`/menus/${id}`),
  createMenu: (menu) => api.post('/menus', menu),
  updateMenu: (id, menu) => api.put(`/menus/${id}`, menu),
  deleteMenu: (id) => api.delete(`/menus/${id}`),
  updateMenuOrder: (menuId, newOrder) => api.put(`/menus/${menuId}/order`, { orderNum: newOrder }),
};

// 권한 관리 API
export const permissionApi = {
  getRoleMenus: (roleId) => api.get(`/permissions/role/${roleId}/menus`),
  updateRoleMenus: (roleId, permissions) => api.put(`/permissions/role/${roleId}/menus`, permissions),
  getPermissionMatrix: () => api.get('/permissions/matrix'),
  getUserPermissions: (userId) => api.get(`/permissions/user/${userId}`),
  checkPermission: (menuCode, action) => api.get(`/permissions/check/${menuCode}/${action}`),
  getPermissionStats: () => api.get('/permissions/stats'),
};

// 로그 관리 API
export const logApi = {
  getLogs: (params = {}) => api.get('/logs', { params }),
  getLogStats: () => api.get('/logs/stats'),
  deleteLogs: (params) => api.delete('/logs', { params }),
  exportLogs: (params) => api.get('/logs/export', { params, responseType: 'blob' }),
};

// 대시보드 API
export const dashboardApi = {
  getDashboardStats: () => api.get('/dashboard/stats'),
  getRecentActivities: () => api.get('/dashboard/activities'),
  getSystemInfo: () => api.get('/dashboard/system-info'),
  getUserStats: () => api.get('/dashboard/user-stats'),
  getMenuStats: () => api.get('/dashboard/menu-stats'),
  getRoleStats: () => api.get('/dashboard/role-stats'),
  getLogStats: () => api.get('/dashboard/log-stats'),
  getActiveUsers: () => api.get('/dashboard/active-users'),
};

// 고급 검색 API
export const searchApi = {
  unifiedSearch: (request) => api.post('/search/unified', request),
  searchUsers: (request) => api.post('/search/users', request),
  searchRoles: (request) => api.post('/search/roles', request),
  searchMenus: (request) => api.post('/search/menus', request),
  suggestUsers: (keyword) => api.get(`/search/autocomplete/users?keyword=${keyword}`),
  suggestRoles: (keyword) => api.get(`/search/autocomplete/roles?keyword=${keyword}`),
  suggestMenus: (keyword) => api.get(`/search/autocomplete/menus?keyword=${keyword}`),
};

// 시스템 모니터링 API
export const monitoringApi = {
  getApiStatistics: () => api.get('/monitoring/api-statistics'),
  getSlowApis: (limit = 10) => api.get(`/monitoring/slow-apis?limit=${limit}`),
  getErrorApis: (limit = 10) => api.get(`/monitoring/error-apis?limit=${limit}`),
  getSystemStatus: () => api.get('/monitoring/system-status'),
  resetStatistics: () => api.post('/monitoring/reset-statistics'),
};


export default api;
