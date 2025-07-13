import { useState, useEffect } from "react";
import {
  Table,
  Card,
  Typography,
  Tag,
  Space,
  DatePicker,
  Select,
  Button,
  Input,
  message,
  Row,
  Col,
  Statistic,
} from "antd";
import {
  FileTextOutlined,
  UserOutlined,
  InfoCircleOutlined,
  WarningOutlined,
  CloseCircleOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import "dayjs/locale/ko";
import logService from "../../services/logService";
import LoadingSpinner from "../../components/common/LoadingSpinner";
import ErrorMessage from "../../components/common/ErrorMessage";
import SkeletonStatistic from "../../components/common/SkeletonStatistic";

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

const LogManagement = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({
    dateRange: null,
    level: null,
    user: null,
    action: null,
    search: "",
  });
  const [logStats, setLogStats] = useState({
    total: 0,
    info: 0,
    warning: 0,
    error: 0,
  });

  useEffect(() => {
    loadLogs();
    loadLogStats();
  }, []);

  const loadLogs = async () => {
    setLoading(true);
    try {
      setError(null);
      let response;

      // 검색 조건이 있는 경우
      if (
        filters.dateRange ||
        filters.level ||
        filters.user ||
        filters.action ||
        filters.search
      ) {
        const searchParams = {
          startDate: filters.dateRange?.[0]?.format("YYYY-MM-DDTHH:mm:ss"),
          endDate: filters.dateRange?.[1]?.format("YYYY-MM-DDTHH:mm:ss"),
          level: filters.level,
          username: filters.user,
          action: filters.action,
          search: filters.search,
          page: 0,
          size: 20,
        };

        response = await logService.searchLogs(searchParams);
      } else {
        // 기본 로그 조회
        response = await logService.getLogs(0, 20);
      }

      const logsData = response.data.data.map((log) => ({
        id: log.id,
        timestamp: dayjs(log.createdAt)
          .locale("ko")
          .format("YYYY. M. D. A h:mm:ss"),
        level: log.level,
        user: log.username,
        action: log.action,
        message: log.message,
        ipAddress: log.ipAddress,
        userAgent: log.userAgent,
      }));

      setLogs(logsData);
    } catch (error) {
      setError(error);
      message.error("로그 데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const loadLogStats = async () => {
    try {
      const response = await logService.getLogStats();
      setLogStats(response.data.data);
    } catch (error) {
      message.error("로그 통계를 불러오는데 실패했습니다.");
    }
  };

  const handleRetry = () => {
    loadLogs();
    loadLogStats();
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleSearch = () => {
    loadLogs();
  };

  const handleReset = async () => {
    // 필터 초기화
    setFilters({
      dateRange: null,
      level: null,
      user: null,
      action: null,
      search: "",
    });

    // 기본 로그 데이터 다시 로드
    setLoading(true);
    try {
      setError(null);
      const response = await logService.getLogs(0, 20);
      const logsData = response.data.data.map((log) => ({
        id: log.id,
        timestamp: dayjs(log.createdAt)
          .locale("ko")
          .format("YYYY. M. D. A h:mm:ss"),
        level: log.level,
        user: log.username,
        action: log.action,
        message: log.message,
        ipAddress: log.ipAddress,
        userAgent: log.userAgent,
      }));

      setLogs(logsData);
      message.success("검색 조건이 초기화되었습니다.");
    } catch (error) {
      setError(error);
      message.error("로그 데이터를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const getLevelColor = (level) => {
    switch (level) {
      case "INFO":
        return "blue";
      case "WARNING":
        return "orange";
      case "ERROR":
        return "red";
      default:
        return "default";
    }
  };

  const getLevelIcon = (level) => {
    switch (level) {
      case "INFO":
        return <InfoCircleOutlined />;
      case "WARNING":
        return <WarningOutlined />;
      case "ERROR":
        return <CloseCircleOutlined />;
      default:
        return <CheckCircleOutlined />;
    }
  };

  const columns = [
    {
      title: "레벨",
      dataIndex: "level",
      key: "level",
      width: 100,
      align: "center",
      render: (level) => (
        <Tag color={getLevelColor(level)} icon={getLevelIcon(level)}>
          {level}
        </Tag>
      ),
    },
    {
      title: "사용자",
      dataIndex: "user",
      key: "user",
      width: 120,
      render: (user) => (
        <Space>
          <UserOutlined />
          <Text>{user}</Text>
        </Space>
      ),
    },
    {
      title: "액션",
      dataIndex: "action",
      key: "action",
      width: 150,
      render: (action) => <Tag color="geekblue">{action}</Tag>,
    },
    {
      title: "메시지",
      dataIndex: "message",
      key: "message",
      render: (message) => <Text>{message}</Text>,
    },
    {
      title: "시간",
      dataIndex: "timestamp",
      key: "timestamp",
      width: 200,
      render: (text) => <Text type="secondary">{text}</Text>,
    },
    {
      title: "IP 주소",
      dataIndex: "ipAddress",
      key: "ipAddress",
      width: 140,
      render: (ip) => <Text code>{ip}</Text>,
    },
  ];

  if (loading && !logs.length) {
    return <LoadingSpinner tip="로그 데이터를 불러오는 중..." />;
  }

  if (error && !logs.length) {
    return (
      <div style={{ padding: "24px" }}>
        <Card>
          <div style={{ marginBottom: "24px" }}>
            <Title level={2} style={{ marginBottom: "8px" }}>
              <FileTextOutlined /> 로그 관리
            </Title>
            <Text type="secondary">
              시스템 로그를 조회하고 모니터링할 수 있습니다.
            </Text>
          </div>
          <ErrorMessage error={error} onRetry={handleRetry} showRetry={true} />
        </Card>
      </div>
    );
  }

  return (
    <div>
      <div
        style={{
          padding: "0 12px 0 12px",
          marginBottom: "24px",
          marginTop: "12px",
        }}
      >
        <h1 style={{ marginBottom: "8px", marginTop: "0px" }}>
          <FileTextOutlined /> 로그 관리
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          시스템 로그를 조회하고 모니터링할 수 있습니다.
        </p>
        <ErrorMessage
          error={error}
          onRetry={handleRetry}
          showRetry={true}
          style={{ margin: "16px 0 0 0" }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        {error && logs.length > 0 && (
          <ErrorMessage
            error={error}
            onRetry={handleRetry}
            type="warning"
            style={{ marginBottom: 16 }}
          />
        )}

        <Card
          style={{
            boxShadow: "none",
            border: "1px solid #f0f0f0",
            marginBottom: 24,
          }}
        >
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} md={8}>
              <div style={{ marginBottom: 8 }}>
                <label style={{ fontWeight: 500 }}>레벨</label>
              </div>
              <Select
                placeholder="레벨 선택"
                value={filters.level}
                onChange={(value) => handleFilterChange("level", value)}
                style={{ width: "100%" }}
                allowClear
              >
                <Option value="INFO">INFO</Option>
                <Option value="WARNING">WARNING</Option>
                <Option value="ERROR">ERROR</Option>
              </Select>
            </Col>
            <Col xs={24} sm={12} md={8}>
              <div style={{ marginBottom: 8 }}>
                <label style={{ fontWeight: 500 }}>사용자</label>
              </div>
              <Input
                placeholder="사용자명 검색"
                value={filters.user}
                onChange={(e) => handleFilterChange("user", e.target.value)}
              />
            </Col>
            <Col xs={24} sm={12} md={8}>
              <div style={{ marginBottom: 8 }}>
                <label style={{ fontWeight: 500 }}>액션</label>
              </div>
              <Input
                placeholder="액션 검색"
                value={filters.action}
                onChange={(e) => handleFilterChange("action", e.target.value)}
              />
            </Col>
          </Row>
          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col xs={24} sm={12} md={8}>
              <div style={{ marginBottom: 8 }}>
                <label style={{ fontWeight: 500 }}>메시지</label>
              </div>
              <Input
                placeholder="메시지 검색"
                value={filters.search}
                onChange={(e) => handleFilterChange("search", e.target.value)}
              />
            </Col>
            <Col xs={24} sm={12} md={8}>
              <div style={{ marginBottom: 8 }}>
                <label style={{ fontWeight: 500 }}>기간</label>
              </div>
              <RangePicker
                placeholder={["시작일", "종료일"]}
                value={filters.dateRange}
                onChange={(dates) => handleFilterChange("dateRange", dates)}
                style={{ width: "100%" }}
              />
            </Col>
          </Row>
          <Row style={{ marginTop: 16 }}>
            <Col xs={24}>
              <div style={{ display: "flex", justifyContent: "flex-end" }}>
                <Space wrap>
                  <Button
                    type="primary"
                    icon={<SearchOutlined />}
                    onClick={handleSearch}
                  >
                    검색
                  </Button>
                  <Button icon={<ReloadOutlined />} onClick={handleReset}>
                    초기화
                  </Button>
                </Space>
              </div>
            </Col>
          </Row>
        </Card>

        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>
          <Row gutter={16} style={{ marginBottom: "24px" }}>
            <Col span={6}>
              <SkeletonStatistic loading={loading && !logs.length}>
                <Statistic
                  title="전체 로그"
                  value={logStats.total}
                  prefix={<FileTextOutlined />}
                />
              </SkeletonStatistic>
            </Col>
            <Col span={6}>
              <SkeletonStatistic loading={loading && !logs.length}>
                <Statistic
                  title="INFO"
                  value={logStats.info}
                  prefix={<InfoCircleOutlined />}
                  valueStyle={{ color: "#1890ff" }}
                />
              </SkeletonStatistic>
            </Col>
            <Col span={6}>
              <SkeletonStatistic loading={loading && !logs.length}>
                <Statistic
                  title="WARNING"
                  value={logStats.warning}
                  prefix={<WarningOutlined />}
                  valueStyle={{ color: "#faad14" }}
                />
              </SkeletonStatistic>
            </Col>
            <Col span={6}>
              <SkeletonStatistic loading={loading && !logs.length}>
                <Statistic
                  title="ERROR"
                  value={logStats.error}
                  prefix={<CloseCircleOutlined />}
                  valueStyle={{ color: "#f5222d" }}
                />
              </SkeletonStatistic>
            </Col>
          </Row>

          <div style={{ marginBottom: "16px" }}>
            <Text strong>로그 목록</Text>
          </div>

          <Table
            columns={columns}
            dataSource={logs}
            rowKey="id"
            loading={loading}
            pagination={{
              showSizeChanger: true,
              showTotal: (total, range) =>
                `${range[0]}-${range[1]} / 총 ${total}개`,
              defaultPageSize: 10,
              pageSizeOptions: ["10", "20", "50", "100"],
            }}
            scroll={{ x: 1200 }}
            size="middle"
          />
        </Card>
      </div>
    </div>
  );
};

export default LogManagement;
