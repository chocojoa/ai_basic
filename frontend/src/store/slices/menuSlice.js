import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import menuService from "../../services/menuService";

export const fetchMenus = createAsyncThunk(
  "menu/fetchMenus",
  async (_, { rejectWithValue }) => {
    try {
      const response = await menuService.getMenus();
      return response.data.data; // 서버 응답 구조에 맞게 수정
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "메뉴 목록을 불러오는데 실패했습니다"
      );
    }
  }
);

export const fetchUserMenus = createAsyncThunk(
  "menu/fetchUserMenus",
  async (_, { rejectWithValue }) => {
    try {
      const response = await menuService.getUserMenus();
      return response.data.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "사용자 메뉴를 불러오는데 실패했습니다"
      );
    }
  }
);

export const createMenu = createAsyncThunk(
  "menu/createMenu",
  async (menuData, { rejectWithValue }) => {
    try {
      const response = await menuService.createMenu(menuData);
      return response.data.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "메뉴 생성에 실패했습니다"
      );
    }
  }
);

export const updateMenu = createAsyncThunk(
  "menu/updateMenu",
  async ({ id, menuData }, { rejectWithValue }) => {
    try {
      const response = await menuService.updateMenu(id, menuData);
      return response.data.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "메뉴 수정에 실패했습니다"
      );
    }
  }
);

export const deleteMenu = createAsyncThunk(
  "menu/deleteMenu",
  async (id, { rejectWithValue }) => {
    try {
      await menuService.deleteMenu(id);
      return id;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || "메뉴 삭제에 실패했습니다"
      );
    }
  }
);

const initialState = {
  menus: [],
  userMenus: [],
  isLoading: false,
  error: null,
};

const menuSlice = createSlice({
  name: "menu",
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearUserMenus: (state) => {
      state.userMenus = [];
    },
    clearAllMenus: (state) => {
      state.menus = [];
      state.userMenus = [];
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMenus.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchMenus.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menus = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchMenus.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(fetchUserMenus.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchUserMenus.fulfilled, (state, action) => {
        state.isLoading = false;
        state.userMenus = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchUserMenus.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(createMenu.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createMenu.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menus.push(action.payload);
      })
      .addCase(createMenu.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(updateMenu.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateMenu.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.menus.findIndex(
          (menu) => menu.id === action.payload.id
        );
        if (index !== -1) {
          state.menus[index] = action.payload;
        }
      })
      .addCase(updateMenu.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(deleteMenu.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteMenu.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menus = state.menus.filter((menu) => menu.id !== action.payload);
      })
      .addCase(deleteMenu.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      });
  },
});

export const { clearError, clearUserMenus } = menuSlice.actions;
export default menuSlice.reducer;
