import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Progress,
  Alert,
  Spin,
  Button,
  Tag,
  Tooltip,
  Space,
  Tabs,
  List,
  Typography,
  Descriptions,
  message
} from 'antd';
import {
  MonitorOutlined,
  ApiOutlined,
  ClockCircleOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ReloadOutlined,
  DesktopOutlined
} from '@ant-design/icons';
import { monitoringApi } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer } from 'recharts';

const { Title, Text } = Typography;

const SystemMonitoring = () => {
  const [loading, setLoading] = useState(false);
  const [apiStats, setApiStats] = useState(null);
  const [systemStatus, setSystemStatus] = useState(null);
  const [slowApis, setSlowApis] = useState([]);
  const [errorApis, setErrorApis] = useState([]);
  const [autoRefresh, setAutoRefresh] = useState(true);

  // 데이터 로드
  const loadData = async () => {
    setLoading(true);
    try {
      const [statsResponse, statusResponse, slowResponse, errorResponse] = await Promise.all([
        monitoringApi.getApiStatistics(),
        monitoringApi.getSystemStatus(),
        monitoringApi.getSlowApis(10),
        monitoringApi.getErrorApis(10)
      ]);

      setApiStats(statsResponse.data);
      setSystemStatus(statusResponse.data);
      setSlowApis(Object.entries(slowResponse.data.slowApis || {}));
      setErrorApis(Object.entries(errorResponse.data.errorApis || {}));
    } catch (error) {
      message.error('모니터링 데이터 로드에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  // 자동 새로고침
  useEffect(() => {
    loadData();
    
    let interval;
    if (autoRefresh) {
      interval = setInterval(() => {
        loadData();
      }, 30000); // 30초마다 새로고침
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [autoRefresh]);

  // 통계 초기화
  const handleResetStats = async () => {
    try {
      await monitoringApi.resetStatistics();
      message.success('통계가 초기화되었습니다');
      loadData();
    } catch (error) {
      message.error('통계 초기화에 실패했습니다');
    }
  };

  // 메모리 사용률 색상
  const getMemoryColor = (usage) => {
    const percent = parseFloat(usage);
    if (percent > 80) return '#ff4d4f';
    if (percent > 60) return '#faad14';
    return '#52c41a';
  };

  // API 응답시간 색상
  const getResponseTimeColor = (time) => {
    if (time > 3000) return 'red';
    if (time > 1000) return 'orange';
    return 'green';
  };

  // 에러율 색상
  const getErrorRateColor = (rate) => {
    if (rate > 10) return 'red';
    if (rate > 5) return 'orange';
    return 'green';
  };

  // 느린 API 테이블 컬럼
  const slowApiColumns = [
    {
      title: 'API 엔드포인트',
      dataIndex: '0',
      key: 'endpoint',
      render: (text) => <code>{text}</code>
    },
    {
      title: '평균 응답시간',
      dataIndex: '1',
      key: 'avgTime',
      render: (data) => (
        <Tag color={getResponseTimeColor(data.averageResponseTime)}>
          {Math.round(data.averageResponseTime)}ms
        </Tag>
      ),
      sorter: (a, b) => a[1].averageResponseTime - b[1].averageResponseTime
    },
    {
      title: '최대 응답시간',
      dataIndex: '1',
      key: 'maxTime',
      render: (data) => (
        <Tag color={getResponseTimeColor(data.maxResponseTime)}>
          {Math.round(data.maxResponseTime)}ms
        </Tag>
      )
    },
    {
      title: '총 요청수',
      dataIndex: '1',
      key: 'totalRequests',
      render: (data) => data.totalRequests.toLocaleString()
    }
  ];

  // 에러 API 테이블 컬럼
  const errorApiColumns = [
    {
      title: 'API 엔드포인트',
      dataIndex: '0',
      key: 'endpoint',
      render: (text) => <code>{text}</code>
    },
    {
      title: '에러율',
      dataIndex: '1',
      key: 'errorRate',
      render: (data) => (
        <Tag color={getErrorRateColor(data.errorRate)}>
          {data.errorRate.toFixed(2)}%
        </Tag>
      ),
      sorter: (a, b) => a[1].errorRate - b[1].errorRate
    },
    {
      title: '에러 횟수',
      dataIndex: '1',
      key: 'errorCount',
      render: (data) => (
        <Text type="danger">{data.errorCount.toLocaleString()}</Text>
      )
    },
    {
      title: '총 요청수',
      dataIndex: '1',
      key: 'totalRequests',
      render: (data) => data.totalRequests.toLocaleString()
    }
  ];

  // API 통계 차트 데이터
  const getApiStatsChartData = () => {
    if (!apiStats?.apis) return [];
    
    return Object.entries(apiStats.apis)
      .slice(0, 10)
      .map(([endpoint, stats]) => ({
        endpoint: endpoint.split(' ')[1] || endpoint, // HTTP 메서드 제거
        responseTime: Math.round(stats.averageResponseTime),
        requests: stats.totalRequests,
        errorRate: stats.errorRate
      }));
  };

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
          <MonitorOutlined /> 시스템 모니터링
        </h1>
        <p
          style={{
            color: "#666",
            margin: 0,
          }}
        >
          API 성능, JVM 메모리 사용량, 시스템 상태 등을 실시간으로 모니터링할 수 있습니다.
        </p>
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        <Card 
          extra={
            <Space>
              <Button
                type={autoRefresh ? 'primary' : 'default'}
                size="small"
                onClick={() => setAutoRefresh(!autoRefresh)}
              >
                자동 새로고침 {autoRefresh ? 'ON' : 'OFF'}
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={loadData}
                loading={loading}
              >
                새로고침
              </Button>
            </Space>
          }
        >
        <Spin spinning={loading}>
          <Tabs 
            defaultActiveKey="api"
            items={[
              {
                key: 'api',
                label: <span><ApiOutlined />API 모니터링</span>,
                children: (
                  <div>
              {/* API 전체 통계 */}
              <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
                <Col xs={24} sm={6}>
                  <Card size="small">
                    <Statistic
                      title="총 요청수"
                      value={apiStats?.overall?.totalRequests || 0}
                      prefix={<ApiOutlined />}
                      valueStyle={{ color: '#1890ff' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={6}>
                  <Card size="small">
                    <Statistic
                      title="활성 요청수"
                      value={apiStats?.overall?.currentActiveRequests || 0}
                      prefix={<ClockCircleOutlined />}
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={6}>
                  <Card size="small">
                    <Statistic
                      title="총 에러수"
                      value={apiStats?.overall?.totalErrors || 0}
                      prefix={<ExclamationCircleOutlined />}
                      valueStyle={{ color: '#ff4d4f' }}
                    />
                  </Card>
                </Col>
                <Col xs={24} sm={6}>
                  <Card size="small">
                    <Statistic
                      title="전체 에러율"
                      value={apiStats?.overall?.errorRate || 0}
                      suffix="%"
                      precision={2}
                      prefix={<WarningOutlined />}
                      valueStyle={{ 
                        color: getErrorRateColor(apiStats?.overall?.errorRate || 0) 
                      }}
                    />
                  </Card>
                </Col>
              </Row>

              {/* API 성능 차트 */}
              {apiStats?.apis && (
                <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
                  <Col span={24}>
                    <Card title="API 평균 응답시간" size="small">
                      <ResponsiveContainer width="100%" height={400}>
                        <BarChart
                          data={getApiStatsChartData()}
                          margin={{
                            top: 20,
                            right: 30,
                            left: 20,
                            bottom: 60,
                          }}
                        >
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis 
                            dataKey="endpoint" 
                            angle={-45}
                            textAnchor="end"
                            height={80}
                            tick={{ fontSize: 11 }}
                            tickFormatter={(value) => {
                              // API 경로를 간단하게 표시
                              const parts = value.split('/');
                              return parts.length > 3 ? `/${parts[2]}/${parts[3]}` : value;
                            }}
                          />
                          <YAxis 
                            label={{ value: '응답시간 (ms)', angle: -90, position: 'insideLeft' }}
                            tick={{ fontSize: 11 }}
                          />
                          <RechartsTooltip 
                            formatter={(value, name) => [`${value}ms`, '응답시간']}
                            labelFormatter={(label) => `API: ${label}`}
                            contentStyle={{
                              backgroundColor: '#fff',
                              border: '1px solid #d9d9d9',
                              borderRadius: '6px'
                            }}
                          />
                          <Bar 
                            dataKey="responseTime" 
                            fill="#1890ff"
                            fillOpacity={0.8}
                          />
                        </BarChart>
                      </ResponsiveContainer>
                    </Card>
                  </Col>
                </Row>
              )}

              {/* 느린 API 및 에러 API */}
              <Row gutter={[16, 16]}>
                <Col xs={24} lg={12}>
                  <Card 
                    title="느린 API TOP 10" 
                    size="small"
                    extra={
                      <Tooltip title="3초 이상 응답시간">
                        <WarningOutlined style={{ color: '#faad14' }} />
                      </Tooltip>
                    }
                  >
                    <Table
                      dataSource={slowApis}
                      columns={slowApiColumns}
                      size="small"
                      pagination={false}
                      scroll={{ x: 400 }}
                      rowKey={(record) => record.endpoint || Math.random()}
                    />
                  </Card>
                </Col>
                <Col xs={24} lg={12}>
                  <Card 
                    title="에러 API TOP 10" 
                    size="small"
                    extra={
                      <Tooltip title="에러율 높은 순">
                        <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />
                      </Tooltip>
                    }
                  >
                    <Table
                      dataSource={errorApis}
                      columns={errorApiColumns}
                      size="small"
                      pagination={false}
                      scroll={{ x: 400 }}
                      rowKey={(record) => record.endpoint || Math.random()}
                    />
                  </Card>
                </Col>
              </Row>

              {/* 통계 초기화 */}
              <Row style={{ marginTop: '24px' }}>
                <Col span={24}>
                  <Alert
                    message="통계 관리"
                    description={
                      <Space>
                        <span>API 통계를 초기화하시겠습니까?</span>
                        <Button 
                          danger 
                          size="small" 
                          onClick={handleResetStats}
                        >
                          통계 초기화
                        </Button>
                      </Space>
                    }
                    type="warning"
                    showIcon
                  />
                </Col>
              </Row>
                  </div>
                )
              },
              {
                key: 'system',
                label: <span><DesktopOutlined />시스템 상태</span>,
                children: (
                  <div>
              {systemStatus && (
                <>
                  {/* JVM 메모리 정보 */}
                  <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
                    <Col span={24}>
                      <Card title="JVM 힙 메모리 사용량" size="small">
                        <Row gutter={[16, 16]}>
                          <Col xs={24} sm={12} md={6}>
                            <Statistic
                              title="사용 중"
                              value={systemStatus.jvm?.usedMemory}
                              valueStyle={{ fontSize: '16px' }}
                            />
                          </Col>
                          <Col xs={24} sm={12} md={6}>
                            <Statistic
                              title="사용 가능"
                              value={systemStatus.jvm?.freeMemory}
                              valueStyle={{ fontSize: '16px' }}
                            />
                          </Col>
                          <Col xs={24} sm={12} md={6}>
                            <Statistic
                              title="커밋된 메모리"
                              value={systemStatus.jvm?.totalMemory}
                              valueStyle={{ fontSize: '16px' }}
                            />
                          </Col>
                          <Col xs={24} sm={12} md={6}>
                            <Statistic
                              title="최대 메모리"
                              value={systemStatus.jvm?.maxMemory}
                              valueStyle={{ fontSize: '16px' }}
                            />
                          </Col>
                        </Row>
                        <div style={{ marginTop: '16px' }}>
                          <Text>힙 메모리 사용률: </Text>
                          <Progress
                            percent={parseFloat(systemStatus.jvm?.memoryUsagePercent)}
                            strokeColor={getMemoryColor(systemStatus.jvm?.memoryUsagePercent)}
                            style={{ marginLeft: '8px' }}
                          />
                        </div>
                      </Card>
                    </Col>
                  </Row>

                  {/* Non-Heap 메모리 및 가비지 컬렉션 */}
                  <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
                    <Col xs={24} lg={12}>
                      <Card title="Non-Heap 메모리 (메타스페이스)" size="small">
                        <Descriptions column={1} size="small">
                          <Descriptions.Item label="사용 중">
                            {systemStatus.jvm?.nonHeapUsed}
                          </Descriptions.Item>
                          <Descriptions.Item label="커밋된 메모리">
                            {systemStatus.jvm?.nonHeapCommitted}
                          </Descriptions.Item>
                          <Descriptions.Item label="최대 메모리">
                            {systemStatus.jvm?.nonHeapMax}
                          </Descriptions.Item>
                        </Descriptions>
                      </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                      <Card title="JVM 정보" size="small">
                        <Descriptions column={1} size="small">
                          <Descriptions.Item label="실행 시간">
                            {systemStatus.jvm?.uptime}
                          </Descriptions.Item>
                          <Descriptions.Item label="시작 시간">
                            {systemStatus.jvm?.startTime ? new Date(systemStatus.jvm.startTime).toLocaleString() : '-'}
                          </Descriptions.Item>
                          <Descriptions.Item label="활성 스레드">
                            {systemStatus.application?.activeThreads} (최대: {systemStatus.application?.peakThreads})
                          </Descriptions.Item>
                        </Descriptions>
                      </Card>
                    </Col>
                  </Row>

                  {/* 시스템 정보 */}
                  <Row gutter={[16, 16]}>
                    <Col xs={24} lg={12}>
                      <Card title="시스템 정보" size="small">
                        <Descriptions column={1} size="small">
                          <Descriptions.Item label="운영체제">
                            {systemStatus.system?.osName} {systemStatus.system?.osVersion} ({systemStatus.system?.osArch})
                          </Descriptions.Item>
                          <Descriptions.Item label="Java 버전">
                            {systemStatus.system?.javaVersion}
                          </Descriptions.Item>
                          <Descriptions.Item label="Java 벤더">
                            {systemStatus.system?.javaVendor}
                          </Descriptions.Item>
                          <Descriptions.Item label="Java Home">
                            {systemStatus.system?.javaHome}
                          </Descriptions.Item>
                          <Descriptions.Item label="CPU 코어">
                            {systemStatus.system?.availableProcessors}개
                          </Descriptions.Item>
                          <Descriptions.Item label="시스템 부하">
                            {systemStatus.system?.systemLoadAverage}
                          </Descriptions.Item>
                        </Descriptions>
                      </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                      <Card title="시스템 상태" size="small">
                        <List
                          size="small"
                          dataSource={[
                            {
                              title: '애플리케이션 상태',
                              status: systemStatus.health?.status === 'UP' ? 'healthy' : 'error',
                              description: `상태: ${systemStatus.health?.status || 'Unknown'}`
                            },
                            {
                              title: '메모리 상태',
                              status: parseFloat(systemStatus.jvm?.memoryUsagePercent) > 80 ? 'warning' : 'healthy',
                              description: `힙 사용률 ${systemStatus.jvm?.memoryUsagePercent}%`
                            },
                            {
                              title: 'HTTP 요청',
                              status: 'healthy',
                              description: `총 ${systemStatus.application?.totalHttpRequests || 0}건`
                            },
                            {
                              title: 'API 상태',
                              status: apiStats?.overall?.errorRate > 10 ? 'error' : 'healthy',
                              description: `에러율 ${apiStats?.overall?.errorRate?.toFixed(2) || 0}%`
                            }
                          ]}
                          renderItem={item => (
                            <List.Item>
                              <List.Item.Meta
                                avatar={
                                  item.status === 'healthy' ? (
                                    <CheckCircleOutlined style={{ color: '#52c41a' }} />
                                  ) : item.status === 'warning' ? (
                                    <WarningOutlined style={{ color: '#faad14' }} />
                                  ) : (
                                    <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />
                                  )
                                }
                                title={item.title}
                                description={item.description}
                              />
                            </List.Item>
                          )}
                        />
                      </Card>
                    </Col>
                  </Row>

                  {/* 가비지 컬렉션 정보 */}
                  {systemStatus.jvm?.gcCollections && Object.keys(systemStatus.jvm.gcCollections).length > 0 && (
                    <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
                      <Col span={24}>
                        <Card title="가비지 컬렉션 통계" size="small">
                          <Row gutter={[16, 16]}>
                            {Object.entries(systemStatus.jvm.gcCollections).map(([gcName, gcStats]) => (
                              <Col xs={24} sm={12} md={8} key={gcName}>
                                <Card size="small" style={{ backgroundColor: '#fafafa' }}>
                                  <Statistic
                                    title={gcName}
                                    value={gcStats.collections}
                                    suffix="회"
                                    precision={0}
                                  />
                                  <Text type="secondary" style={{ fontSize: '12px' }}>
                                    총 시간: {gcStats.time}
                                  </Text>
                                </Card>
                              </Col>
                            ))}
                          </Row>
                        </Card>
                      </Col>
                    </Row>
                  )}
                </>
              )}
                  </div>
                )
              }
            ]}
          />
        </Spin>
        </Card>
      </div>
    </div>
  );
};

export default SystemMonitoring;