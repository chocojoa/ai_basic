import { useEffect } from "react";
import { RouterProvider } from "react-router-dom";
import { Provider, useDispatch } from "react-redux";
import { ConfigProvider } from "antd";
import koKR from "antd/locale/ko_KR";
import store from "./store/store";
import { initializeAuth } from "./store/slices/authSlice";
import router from "./routes/router";
import ErrorBoundary from "./components/common/ErrorBoundary";
import "./App.css";

function AppInner() {
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(initializeAuth());
  }, [dispatch]);

  return (
    <ConfigProvider locale={koKR}>
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}

function App() {
  return (
    <Provider store={store}>
      <ErrorBoundary>
        <AppInner />
      </ErrorBoundary>
    </Provider>
  );
}

export default App;
