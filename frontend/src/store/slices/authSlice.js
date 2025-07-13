import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import authService from "../../services/authService";

// 초기 상태를 가져오는 함수
const getStoredAuth = () => {
  try {
    const token = localStorage.getItem("token");
    const refreshToken = localStorage.getItem("refreshToken");
    const userStr = localStorage.getItem("user");
    
    if (token && refreshToken) {
      return {
        token,
        refreshToken,
        user: userStr ? JSON.parse(userStr) : null,
        isAuthenticated: true,
        isInitialized: false, // 아직 서버와 동기화되지 않음
      };
    }
  } catch (error) {
  }
  
  return {
    token: null,
    refreshToken: null,
    user: null,
    isAuthenticated: false,
    isInitialized: true,
  };
};

// 로그인
export const login = createAsyncThunk(
  "auth/login",
  async ({ username, password }, { rejectWithValue }) => {
    try {
      const response = await authService.login(username, password);      
      
      // 다양한 응답 형식 처리
      let loginData = response.data;
      
      // response.data.data 형식 처리
      if (loginData.data && typeof loginData.data === 'object') {
        loginData = loginData.data;
      }
      
      // accessToken을 token으로 매핑
      if (loginData.accessToken && !loginData.token) {
        loginData.token = loginData.accessToken;
      }

      // 토큰과 사용자 정보 저장
      if (loginData.token) {
        localStorage.setItem("token", loginData.token);
      } else {
        throw new Error("No token received from server");
      }
      
      if (loginData.refreshToken) {
        localStorage.setItem("refreshToken", loginData.refreshToken);
      }
      
      // 사용자 정보 저장 (새로운 응답 구조에서 user 객체 사용)
      const userData = loginData.user || {
        id: loginData.id,
        username: loginData.username || username,
        email: loginData.email,
        fullName: loginData.fullName,
        roles: loginData.roles
      };
      
      if (userData) {
        localStorage.setItem("user", JSON.stringify(userData));
      }

      return {
        token: loginData.token,
        refreshToken: loginData.refreshToken,
        user: userData
      };
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "로그인에 실패했습니다"
      );
    }
  }
);

// 로그아웃
export const logout = createAsyncThunk(
  "auth/logout",
  async (_, { rejectWithValue }) => {
    try {
      await authService.logout();
    } catch (error) {
    } finally {
      // API 호출 성공 여부와 관계없이 로컬 스토리지 정리
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
    }
  }
);

// 토큰 갱신
export const refreshToken = createAsyncThunk(
  "auth/refreshToken",
  async (_, { getState, rejectWithValue }) => {
    try {
      const { auth } = getState();
      const refreshTokenValue = auth.refreshToken || localStorage.getItem("refreshToken");
      
      if (!refreshTokenValue) {
        throw new Error("Refresh token not found");
      }
      
      const response = await authService.refreshToken(refreshTokenValue);
      const data = response.data.data || response.data;
      
      // 새 토큰 저장
      if (data.token) {
        localStorage.setItem("token", data.token);
      }
      if (data.refreshToken) {
        localStorage.setItem("refreshToken", data.refreshToken);
      }
      
      return data;
    } catch (error) {
      // 토큰 갱신 실패 시 정리
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("user");
      
      return rejectWithValue(
        error.response?.data?.message || "토큰 갱신에 실패했습니다"
      );
    }
  }
);

// 현재 사용자 정보 가져오기
export const getCurrentUser = createAsyncThunk(
  "auth/getCurrentUser",
  async (_, { rejectWithValue }) => {
    try {
      const response = await authService.getCurrentUser();
      const userData = response.data.data || response.data;
      
      // 사용자 정보 업데이트
      if (userData) {
        localStorage.setItem("user", JSON.stringify(userData));
      }
      
      return userData;
    } catch (error) {
      // 401 에러인 경우에만 토큰 제거
      if (error.response?.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");
      }
      
      return rejectWithValue(
        error.response?.data?.message || "사용자 정보 조회에 실패했습니다"
      );
    }
  }
);

// 인증 초기화 (앱 시작 시 호출)
export const initializeAuth = createAsyncThunk(
  "auth/initialize",
  async (_, { dispatch, rejectWithValue }) => {
    try {
      const token = localStorage.getItem("token");
      
      if (!token) {
        return { isAuthenticated: false };
      }
      
      // 사용자 정보 가져오기
      const result = await dispatch(getCurrentUser()).unwrap();
      return { isAuthenticated: true, user: result };
    } catch (error) {
      // getCurrentUser 실패 시
      return rejectWithValue(error);
    }
  }
);

const initialState = {
  ...getStoredAuth(),
  isLoading: false,
  error: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setCredentials: (state, action) => {
      state.user = action.payload.user;
      state.token = action.payload.token;
      state.refreshToken = action.payload.refreshToken;
      state.isAuthenticated = true;
      state.isInitialized = true;
    },
    clearCredentials: (state) => {
      state.user = null;
      state.token = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
      state.isInitialized = true;
    },
  },
  extraReducers: (builder) => {
    builder
      // 로그인
      .addCase(login.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user || action.payload;
        state.token = action.payload.token;
        state.refreshToken = action.payload.refreshToken;
        state.isAuthenticated = true;
        state.isInitialized = true;
        state.error = null;
      })
      .addCase(login.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
        state.isAuthenticated = false;
        state.isInitialized = true;
      })
      
      // 로그아웃
      .addCase(logout.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(logout.fulfilled, (state) => {
        state.isLoading = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.isInitialized = true;
        state.error = null;
      })
      .addCase(logout.rejected, (state) => {
        // 로그아웃 실패해도 로컬 상태는 정리
        state.isLoading = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.isInitialized = true;
      })
      
      // 토큰 갱신
      .addCase(refreshToken.fulfilled, (state, action) => {
        state.token = action.payload.token;
        if (action.payload.user) {
          state.user = action.payload.user;
        }
      })
      .addCase(refreshToken.rejected, (state) => {
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.isInitialized = true;
      })
      
      // 현재 사용자 정보 가져오기
      .addCase(getCurrentUser.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(getCurrentUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
        state.isInitialized = true;
        state.error = null;
      })
      .addCase(getCurrentUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
        
        // 401 에러인 경우에만 인증 정보 제거
        if (!localStorage.getItem("token")) {
          state.user = null;
          state.token = null;
          state.refreshToken = null;
          state.isAuthenticated = false;
        }
        state.isInitialized = true;
      })
      
      // 인증 초기화
      .addCase(initializeAuth.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(initializeAuth.fulfilled, (state, action) => {
        state.isLoading = false;
        state.isAuthenticated = action.payload.isAuthenticated;
        if (action.payload.user) {
          state.user = action.payload.user;
        }
        state.isInitialized = true;
        state.error = null;
      })
      .addCase(initializeAuth.rejected, (state) => {
        state.isLoading = false;
        state.user = null;
        state.token = null;
        state.refreshToken = null;
        state.isAuthenticated = false;
        state.isInitialized = true;
      });
  },
});

export const { clearError, setCredentials, clearCredentials } = authSlice.actions;
export default authSlice.reducer;
