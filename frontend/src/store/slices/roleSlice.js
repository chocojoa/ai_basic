import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import roleService from '../../services/roleService';

export const fetchRoles = createAsyncThunk(
  'role/fetchRoles',
  async (_, { rejectWithValue }) => {
    try {
      const response = await roleService.getRoles();
      return response.data?.data || response.data || [];
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || '역할 목록을 불러오는데 실패했습니다');
    }
  }
);

export const createRole = createAsyncThunk(
  'role/createRole',
  async (roleData, { rejectWithValue }) => {
    try {
      const response = await roleService.createRole(roleData);
      return response.data?.data || response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || '역할 생성에 실패했습니다');
    }
  }
);

export const updateRole = createAsyncThunk(
  'role/updateRole',
  async ({ id, roleData }, { rejectWithValue }) => {
    try {
      const response = await roleService.updateRole(id, roleData);
      return response.data?.data || response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || '역할 수정에 실패했습니다');
    }
  }
);

export const deleteRole = createAsyncThunk(
  'role/deleteRole',
  async (id, { rejectWithValue }) => {
    try {
      await roleService.deleteRole(id);
      return id;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || '역할 삭제에 실패했습니다');
    }
  }
);

const initialState = {
  roles: [],
  isLoading: false,
  error: null,
};

const roleSlice = createSlice({
  name: 'role',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchRoles.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchRoles.fulfilled, (state, action) => {
        state.isLoading = false;
        state.roles = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchRoles.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(createRole.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createRole.fulfilled, (state, action) => {
        state.isLoading = false;
        if (action.payload) {
          state.roles.push(action.payload);
        }
      })
      .addCase(createRole.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(updateRole.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateRole.fulfilled, (state, action) => {
        state.isLoading = false;
        if (action.payload) {
          const index = state.roles.findIndex(role => role.id === action.payload.id);
          if (index !== -1) {
            state.roles[index] = action.payload;
          }
        }
      })
      .addCase(updateRole.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      .addCase(deleteRole.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteRole.fulfilled, (state, action) => {
        state.isLoading = false;
        state.roles = state.roles.filter(role => role.id !== action.payload);
      })
      .addCase(deleteRole.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      });
  },
});

export const { clearError } = roleSlice.actions;
export default roleSlice.reducer;