# Frontend 개발자 가이드

## 목차
1. [개발 환경 설정](#개발-환경-설정)
2. [프로젝트 구조](#프로젝트-구조)
3. [개발 워크플로우](#개발-워크플로우)
4. [컴포넌트 개발 가이드](#컴포넌트-개발-가이드)
5. [상태 관리](#상태-관리)
6. [API 통신](#api-통신)
7. [라우팅](#라우팅)
8. [스타일링](#스타일링)
9. [테스트](#테스트)
10. [빌드 및 배포](#빌드-및-배포)
11. [성능 최적화](#성능-최적화)
12. [트러블슈팅](#트러블슈팅)

## 개발 환경 설정

### 필수 도구
- **Node.js**: 18.x 이상
- **npm**: 9.x 이상
- **VS Code**: 권장 IDE

### 권장 VS Code 확장
```json
{
  "recommendations": [
    "bradlc.vscode-tailwindcss",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "ms-vscode.vscode-typescript-next",
    "formulahendry.auto-rename-tag",
    "christian-kohler.path-intellisense"
  ]
}
```

### 프로젝트 설정
```bash
cd frontend
npm install
npm run dev
```

### 환경 변수 설정
`.env` 파일 생성:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Basic Project
VITE_APP_VERSION=1.0.0
```

## 프로젝트 구조

```
frontend/
├── public/                 # 정적 파일
├── src/
│   ├── components/         # 재사용 가능한 컴포넌트
│   │   ├── common/        # 공통 컴포넌트
│   │   ├── layout/        # 레이아웃 컴포넌트
│   │   └── ui/            # UI 컴포넌트
│   ├── pages/             # 페이지 컴포넌트
│   │   ├── auth/          # 인증 관련 페이지
│   │   ├── admin/         # 관리자 페이지
│   │   └── dashboard/     # 대시보드 페이지
│   ├── services/          # API 서비스
│   ├── store/             # Redux 상태 관리
│   │   ├── slices/        # Redux 슬라이스
│   │   └── middleware/    # 미들웨어
│   ├── utils/             # 유틸리티 함수
│   ├── hooks/             # 커스텀 훅
│   ├── constants/         # 상수 정의
│   ├── types/             # TypeScript 타입 정의
│   └── styles/            # 스타일 파일
├── tests/                 # 테스트 파일
└── docs/                  # 문서
```

## 개발 워크플로우

### 1. 새로운 기능 개발
```bash
# 1. 브랜치 생성
git checkout -b feature/새기능명

# 2. 개발 서버 실행
npm run dev

# 3. 개발 진행
# 4. 테스트 실행
npm test

# 5. 빌드 테스트
npm run build

# 6. 커밋 및 푸시
git add .
git commit -m "feat: 새기능 추가"
git push origin feature/새기능명
```

### 2. 코드 품질 검사
```bash
# ESLint 검사
npm run lint

# Prettier 포맷팅
npm run format

# 타입 검사 (TypeScript 사용 시)
npm run type-check
```

## 컴포넌트 개발 가이드

### 컴포넌트 구조
```jsx
// components/common/Button/Button.jsx
import React from 'react';
import { Button as AntButton } from 'antd';
import PropTypes from 'prop-types';
import './Button.scss';

const Button = ({ 
  children, 
  variant = 'primary', 
  size = 'medium',
  disabled = false,
  loading = false,
  onClick,
  ...props 
}) => {
  const getButtonType = () => {
    switch (variant) {
      case 'primary': return 'primary';
      case 'secondary': return 'default';
      case 'danger': return 'danger';
      default: return 'default';
    }
  };

  return (
    <AntButton
      type={getButtonType()}
      size={size}
      disabled={disabled}
      loading={loading}
      onClick={onClick}
      className={`custom-button custom-button--${variant}`}
      {...props}
    >
      {children}
    </AntButton>
  );
};

Button.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf(['primary', 'secondary', 'danger']),
  size: PropTypes.oneOf(['small', 'medium', 'large']),
  disabled: PropTypes.bool,
  loading: PropTypes.bool,
  onClick: PropTypes.func
};

export default Button;
```

### 컴포넌트 명명 규칙
- **PascalCase**: 컴포넌트 이름
- **camelCase**: props, 함수명
- **kebab-case**: CSS 클래스명

### 폴더 구조 예시
```
components/
├── common/
│   ├── Button/
│   │   ├── Button.jsx
│   │   ├── Button.test.js
│   │   ├── Button.scss
│   │   └── index.js
│   └── Modal/
│       ├── Modal.jsx
│       ├── Modal.test.js
│       ├── Modal.scss
│       └── index.js
```

## 상태 관리

### Redux Toolkit 설정
```javascript
// store/index.js
import { configureStore } from '@reduxjs/toolkit';
import authSlice from './slices/authSlice';
import userSlice from './slices/userSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    user: userSlice,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

### 슬라이스 생성 예시
```javascript
// store/slices/authSlice.js
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authService } from '../../services/authService';

// 비동기 액션
export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response.data);
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: null,
    token: localStorage.getItem('token'),
    isLoading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      localStorage.removeItem('token');
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.isLoading = false;
        state.user = action.payload.user;
        state.token = action.payload.token;
        localStorage.setItem('token', action.payload.token);
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload.message;
      });
  },
});

export const { logout, clearError } = authSlice.actions;
export default authSlice.reducer;
```

### 컴포넌트에서 상태 사용
```jsx
// components/LoginForm.jsx
import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { loginUser } from '../store/slices/authSlice';

const LoginForm = () => {
  const dispatch = useDispatch();
  const { isLoading, error } = useSelector((state) => state.auth);
  const [formData, setFormData] = useState({ username: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(loginUser(formData));
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* 폼 내용 */}
    </form>
  );
};
```

## API 통신

### API 서비스 구조
```javascript
// services/api.js
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 서비스 클래스 예시
```javascript
// services/userService.js
import api from './api';

export const userService = {
  // 사용자 목록 조회
  getUsers: (params = {}) => {
    return api.get('/users', { params });
  },

  // 사용자 상세 조회
  getUserById: (id) => {
    return api.get(`/users/${id}`);
  },

  // 사용자 생성
  createUser: (userData) => {
    return api.post('/users', userData);
  },

  // 사용자 수정
  updateUser: (id, userData) => {
    return api.put(`/users/${id}`, userData);
  },

  // 사용자 삭제
  deleteUser: (id) => {
    return api.delete(`/users/${id}`);
  },
};
```

### 커스텀 훅 활용
```javascript
// hooks/useUsers.js
import { useState, useEffect } from 'react';
import { userService } from '../services/userService';

export const useUsers = (filters = {}) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await userService.getUsers(filters);
      setUsers(response.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [JSON.stringify(filters)]);

  return { users, loading, error, refetch: fetchUsers };
};
```

## 라우팅

### React Router 설정
```jsx
// App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store';
import ProtectedRoute from './components/common/ProtectedRoute';
import Layout from './components/layout/Layout';
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import UsersPage from './pages/admin/UsersPage';

function App() {
  return (
    <Provider store={store}>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <Layout>
                  <Routes>
                    <Route path="/" element={<DashboardPage />} />
                    <Route path="/admin/users" element={<UsersPage />} />
                  </Routes>
                </Layout>
              </ProtectedRoute>
            }
          />
        </Routes>
      </Router>
    </Provider>
  );
}

export default App;
```

### 보호된 라우트
```jsx
// components/common/ProtectedRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

const ProtectedRoute = ({ children }) => {
  const { token } = useSelector((state) => state.auth);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
```

## 스타일링

### Ant Design 테마 커스터마이징
```javascript
// main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import App from './App.jsx';

const theme = {
  token: {
    colorPrimary: '#1890ff',
    borderRadius: 6,
    colorBgContainer: '#ffffff',
  },
  components: {
    Button: {
      borderRadius: 6,
    },
  },
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <ConfigProvider theme={theme} locale={koKR}>
    <App />
  </ConfigProvider>
);
```

### SCSS 변수
```scss
// styles/variables.scss
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$error-color: #ff4d4f;

$border-radius-base: 6px;
$border-radius-sm: 4px;
$border-radius-lg: 8px;

$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;
```

## 테스트

### 테스트 환경 설정
```javascript
// tests/setup.js
import '@testing-library/jest-dom';
import { configure } from '@testing-library/react';

configure({ testIdAttribute: 'data-testid' });
```

### 컴포넌트 테스트 예시
```javascript
// components/common/Button/Button.test.js
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import Button from './Button';

describe('Button Component', () => {
  it('renders correctly', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('handles click events', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('applies correct variant class', () => {
    render(<Button variant="danger">Delete</Button>);
    expect(screen.getByText('Delete')).toHaveClass('custom-button--danger');
  });
});
```

### Redux 테스트
```javascript
// store/slices/authSlice.test.js
import { configureStore } from '@reduxjs/toolkit';
import authSlice, { loginUser } from './authSlice';

describe('authSlice', () => {
  let store;

  beforeEach(() => {
    store = configureStore({
      reducer: {
        auth: authSlice,
      },
    });
  });

  it('should handle initial state', () => {
    expect(store.getState().auth.user).toBeNull();
  });

  it('should handle loginUser.fulfilled', () => {
    const mockUser = { id: 1, username: 'test' };
    const mockToken = 'mock-token';

    store.dispatch(
      loginUser.fulfilled({ user: mockUser, token: mockToken })
    );

    const state = store.getState().auth;
    expect(state.user).toEqual(mockUser);
    expect(state.token).toBe(mockToken);
  });
});
```

## 빌드 및 배포

### 개발 환경
```bash
npm run dev
```

### 프로덕션 빌드
```bash
npm run build
npm run preview  # 빌드 결과 미리보기
```

### 환경별 설정
```javascript
// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: process.env.NODE_ENV === 'development',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          antd: ['antd'],
        },
      },
    },
  },
});
```

## 성능 최적화

### 코드 분할
```jsx
// 동적 임포트
const LazyComponent = React.lazy(() => import('./LazyComponent'));

function App() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <LazyComponent />
    </Suspense>
  );
}
```

### 메모이제이션
```jsx
// React.memo
const ExpensiveComponent = React.memo(({ data }) => {
  return <div>{/* 복잡한 렌더링 로직 */}</div>;
});

// useMemo
const MemoizedComponent = () => {
  const expensiveValue = useMemo(() => {
    return heavyCalculation(data);
  }, [data]);

  return <div>{expensiveValue}</div>;
};

// useCallback
const CallbackComponent = () => {
  const handleClick = useCallback(() => {
    // 이벤트 핸들러 로직
  }, [dependency]);

  return <Button onClick={handleClick}>Click</Button>;
};
```

### 번들 크기 최적화
```bash
# 번들 분석
npm run build
npm install -g serve
serve -s dist

# 번들 분석기 설치
npm install --save-dev rollup-plugin-visualizer
```

## 트러블슈팅

### 일반적인 문제들

#### 1. CORS 에러
```javascript
// vite.config.js에서 프록시 설정
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

#### 2. 메모리 누수
```jsx
// useEffect 클린업
useEffect(() => {
  const subscription = subscribe();
  
  return () => {
    subscription.unsubscribe();
  };
}, []);
```

#### 3. 렌더링 성능 문제
```jsx
// 불필요한 리렌더링 방지
const OptimizedComponent = React.memo(({ items }) => {
  return (
    <ul>
      {items.map(item => (
        <li key={item.id}>{item.name}</li>
      ))}
    </ul>
  );
});
```

### 디버깅 도구
- **React Developer Tools**: 컴포넌트 상태 확인
- **Redux DevTools**: 상태 변화 추적
- **Network Tab**: API 요청 모니터링
- **Performance Tab**: 렌더링 성능 분석

### 로그 시스템
```javascript
// utils/logger.js
const logger = {
  info: (message, data) => {
    if (process.env.NODE_ENV === 'development') {
      console.log('[INFO]', message, data);
    }
  },
  error: (message, error) => {
    console.error('[ERROR]', message, error);
    // 프로덕션에서는 에러 리포팅 서비스로 전송
  },
};

export default logger;
```