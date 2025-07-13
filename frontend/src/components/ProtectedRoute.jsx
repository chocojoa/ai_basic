import { Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { Spin } from "antd";

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isInitialized, isLoading } = useSelector(
    (state) => state.auth
  );

  // 아직 초기화되지 않았거나 로딩 중이면 스피너 표시
  if (!isInitialized || isLoading) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
        }}
      >
        <Spin size="large" />
        <div style={{ marginTop: "16px", color: "#666" }}>Loading...</div>
      </div>
    );
  }

  // 초기화가 완료되었고 인증되지 않았으면 로그인 페이지로
  if (isInitialized && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // 인증되었으면 children 렌더링
  return children;
};

export default ProtectedRoute;
