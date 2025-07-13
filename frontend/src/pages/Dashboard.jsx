import React, { useEffect, useState } from "react";
import { Card, Row, Col, Space, Spin, message } from "antd";
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  DashboardOutlined,
} from "@ant-design/icons";
import dashboardService from "../services/dashboardService";
import ErrorMessage from "../components/common/ErrorMessage";

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [recentActivities, setRecentActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 색상 팔레트 정의
  const colors = {
    white: "#ffffff",
    lightGray: "#f8f9fa",
    gray: "#6c757d",
    darkGray: "#343a40",
    black: "#000000",
    coral: "#ff6b6b",
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      // 통계 데이터 로드
      const statsResponse = await dashboardService.getStats();
      setStats(statsResponse.data.data);

      // 최근 로그 데이터 로드
      const logsResponse = await dashboardService.getRecentLogs(10);
      const logs = logsResponse.data.data || [];

      // 로그를 활동 형태로 변환
      const activities = logs.map((log, index) => ({
        key: log.id || index,
        user: log.username || "시스템",
        action: getActionText(log.action),
        target: log.message,
        time: formatTime(log.createdAt),
        type: getLogType(log.level),
      }));

      setRecentActivities(activities);
    } catch (error) {
      setError(error);
      message.error("대시보드 데이터를 불러오는데 실패했습니다");
    } finally {
      setLoading(false);
    }
  };

  const getActionText = (action) => {
    const actionMap = {
      LOGIN: "로그인",
      LOGOUT: "로그아웃",
      CREATE_USER: "사용자 생성",
      UPDATE_USER: "사용자 수정",
      DELETE_USER: "사용자 삭제",
      CREATE_ROLE: "역할 생성",
      UPDATE_ROLE: "역할 수정",
      DELETE_ROLE: "역할 삭제",
      UPDATE_PERMISSION: "권한 수정",
      CREATE_MENU: "메뉴 생성",
      UPDATE_MENU: "메뉴 수정",
      DELETE_MENU: "메뉴 삭제",
    };
    return actionMap[action] || action;
  };

  const getLogType = (level) => {
    const typeMap = {
      INFO: "info",
      WARNING: "warning",
      ERROR: "error",
    };
    return typeMap[level] || "info";
  };

  const formatTime = (timestamp) => {
    const now = new Date();
    const logTime = new Date(timestamp);
    const diffInMinutes = Math.floor((now - logTime) / (1000 * 60));

    if (diffInMinutes < 1) return "방금 전";
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}시간 전`;
    return `${Math.floor(diffInMinutes / 1440)}일 전`;
  };

  if (loading) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "50vh",
        }}
      >
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ minHeight: "calc(100vh - 122px)" }}>
      <div
        style={{
          padding: "0 12px 0 12px",
          marginBottom: "24px",
          marginTop: "12px",
        }}
      >
        <h1
          style={{
            marginBottom: "8px",
            marginTop: "0px",
          }}
        >
          <DashboardOutlined /> 대시보드
        </h1>
        <p
          style={{
            color: "#666",
            margin: 0,
          }}
        >
          시스템의 전반적인 현황과 최근 활동을 모니터링할 수 있습니다.
        </p>
        <ErrorMessage
          error={error}
          onRetry={() => {
            setError(null);
            loadDashboardData();
          }}
          showRetry={true}
          style={{ margin: "16px 0 0 0" }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        <Row gutter={[24, 24]} style={{ marginBottom: 32 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                backgroundColor: colors.white,
                border: "1px solid #d9d9d9",
                borderRadius: 12,
                boxShadow: "none",
              }}
              styles={{ body: { padding: "24px" } }}
            >
              <div>
                <div
                  style={{
                    fontSize: "36px",
                    fontWeight: 700,
                    color: colors.black,
                    marginBottom: 8,
                    lineHeight: 1,
                  }}
                >
                  {loading ? "-" : (stats?.totalUsers || 1250).toLocaleString()}
                </div>
                <div
                  style={{
                    fontSize: "14px",
                    color: colors.gray,
                    marginBottom: 12,
                    fontWeight: 500,
                  }}
                >
                  전체 사용자
                </div>
                <div
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    backgroundColor: "#1677ff",
                    color: colors.white,
                    padding: "4px 8px",
                    borderRadius: 6,
                    fontSize: "12px",
                    fontWeight: 600,
                  }}
                >
                  <ArrowUpOutlined
                    style={{ marginRight: 4, fontSize: "10px" }}
                  />
                  +12.5%
                </div>
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                backgroundColor: colors.white,
                border: "1px solid #d9d9d9",
                borderRadius: 12,
                boxShadow: "none",
              }}
              styles={{ body: { padding: "24px" } }}
            >
              <div>
                <div
                  style={{
                    fontSize: "36px",
                    fontWeight: 700,
                    color: colors.black,
                    marginBottom: 8,
                    lineHeight: 1,
                  }}
                >
                  {loading
                    ? "-"
                    : (stats?.activeUsers || 1234).toLocaleString()}
                </div>
                <div
                  style={{
                    fontSize: "14px",
                    color: colors.gray,
                    marginBottom: 12,
                    fontWeight: 500,
                  }}
                >
                  활성 사용자
                </div>
                <div
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    backgroundColor: "#1677ff",
                    color: colors.white,
                    padding: "4px 8px",
                    borderRadius: 6,
                    fontSize: "12px",
                    fontWeight: 600,
                  }}
                >
                  <ArrowDownOutlined
                    style={{ marginRight: 4, fontSize: "10px" }}
                  />
                  -2.1%
                </div>
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                backgroundColor: colors.white,
                border: "1px solid #d9d9d9",
                borderRadius: 12,
                boxShadow: "none",
              }}
              styles={{ body: { padding: "24px" } }}
            >
              <div>
                <div
                  style={{
                    fontSize: "36px",
                    fontWeight: 700,
                    color: colors.black,
                    marginBottom: 8,
                    lineHeight: 1,
                  }}
                >
                  {loading
                    ? "-"
                    : (stats?.totalRoles || 45678).toLocaleString()}
                </div>
                <div
                  style={{
                    fontSize: "14px",
                    color: colors.gray,
                    marginBottom: 12,
                    fontWeight: 500,
                  }}
                >
                  총 세션
                </div>
                <div
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    backgroundColor: "#1677ff",
                    color: colors.white,
                    padding: "4px 8px",
                    borderRadius: 6,
                    fontSize: "12px",
                    fontWeight: 600,
                  }}
                >
                  <ArrowUpOutlined
                    style={{ marginRight: 4, fontSize: "10px" }}
                  />
                  +8.2%
                </div>
              </div>
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                backgroundColor: colors.white,
                border: "1px solid #d9d9d9",
                borderRadius: 12,
                boxShadow: "none",
              }}
              styles={{ body: { padding: "24px" } }}
            >
              <div>
                <div
                  style={{
                    fontSize: "36px",
                    fontWeight: 700,
                    color: colors.black,
                    marginBottom: 8,
                    lineHeight: 1,
                  }}
                >
                  {loading ? "-" : `${stats?.totalMenus || 4.5}%`}
                </div>
                <div
                  style={{
                    fontSize: "14px",
                    color: colors.gray,
                    marginBottom: 12,
                    fontWeight: 500,
                  }}
                >
                  성장률
                </div>
                <div
                  style={{
                    display: "inline-flex",
                    alignItems: "center",
                    backgroundColor: "#1677ff",
                    color: colors.white,
                    padding: "4px 8px",
                    borderRadius: 6,
                    fontSize: "12px",
                    fontWeight: 600,
                  }}
                >
                  <ArrowUpOutlined
                    style={{ marginRight: 4, fontSize: "10px" }}
                  />
                  +0.3%
                </div>
              </div>
            </Card>
          </Col>
        </Row>

        <Row gutter={[24, 24]}>
          <Col span={24}>
            <Card
              style={{
                backgroundColor: colors.white,
                border: "1px solid #d9d9d9",
                borderRadius: 12,
                boxShadow: "none",
              }}
              styles={{ body: { padding: "32px" } }}
            >
              <div style={{ marginBottom: 32 }}>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: 8,
                  }}
                >
                  <h3
                    style={{
                      margin: 0,
                      fontSize: "20px",
                      fontWeight: 600,
                      color: colors.black,
                    }}
                  >
                    활동 개요
                  </h3>
                  <Space>
                    <div
                      style={{
                        background: "#ffffff",
                        border: "1px solid #d9d9d9",
                        color: colors.gray,
                        borderRadius: 8,
                        padding: "8px 16px",
                        fontSize: "14px",
                        fontWeight: 500,
                      }}
                    >
                      최근 3개월
                    </div>
                    <div
                      style={{
                        background: "#ffffff",
                        border: "1px solid #d9d9d9",
                        color: colors.gray,
                        borderRadius: 8,
                        padding: "8px 16px",
                        fontSize: "14px",
                        fontWeight: 500,
                      }}
                    >
                      최근 30일
                    </div>
                    <div
                      style={{
                        background: "#1677ff",
                        border: "none",
                        color: "#ffffff",
                        borderRadius: 8,
                        padding: "8px 16px",
                        fontSize: "14px",
                        fontWeight: 500,
                      }}
                    >
                      최근 7일
                    </div>
                  </Space>
                </div>
                <div
                  style={{
                    fontSize: "14px",
                    color: colors.gray,
                    fontWeight: 500,
                  }}
                >
                  선택된 기간의 시스템 활동
                </div>
              </div>

              <div
                style={{
                  height: 240,
                  backgroundColor: colors.lightGray,
                  borderRadius: 12,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  border: "none",
                }}
              >
                <div
                  style={{
                    textAlign: "center",
                    color: colors.gray,
                  }}
                >
                  <div
                    style={{
                      fontSize: "18px",
                      marginBottom: 8,
                      fontWeight: 600,
                    }}
                  >
                    📊 분석 차트
                  </div>
                  <div
                    style={{
                      fontSize: "14px",
                      fontWeight: 500,
                    }}
                  >
                    차트 라이브러리 연동 대기 중
                  </div>
                </div>
              </div>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default Dashboard;
