import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Switch,
  Space,
  Popconfirm,
  message,
  Card,
  Typography,
  Tag,
  Tooltip,
  Row,
  Col,
  Statistic,
  Divider,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  SafetyOutlined,
  CheckCircleOutlined,
  StopOutlined,
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import {
  fetchRoles,
  createRole,
  updateRole,
  deleteRole,
  clearError,
} from "../../store/slices/roleSlice";
import roleService from "../../services/roleService";
import SkeletonStatistic from "../../components/common/SkeletonStatistic";
import ErrorMessage from "../../components/common/ErrorMessage";

const { Title, Text } = Typography;

const RoleManagement = () => {
  const dispatch = useDispatch();
  const { roles, isLoading, error } = useSelector((state) => state.role);

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [form] = Form.useForm();
  const [roleStats, setRoleStats] = useState({
    total: 0,
    active: 0,
    inactive: 0,
  });

  useEffect(() => {
    dispatch(fetchRoles());
    loadRoleStats();
  }, [dispatch]);

  useEffect(() => {
    if (error) {
      message.error(error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  useEffect(() => {
    if (roles) {
      setRoleStats({
        total: roles.length,
        active: roles.filter((role) => role.isActive).length,
        inactive: roles.filter((role) => !role.isActive).length,
      });
    }
  }, [roles]);

  const loadRoleStats = async () => {
    try {
      const response = await roleService.getRoleCount();
      const activeRoles = await roleService.getActiveRoles();
      const inactiveRoles = await roleService.getInactiveRoles();

      setRoleStats({
        total: response.data.data,
        active: activeRoles.data.data.length,
        inactive: inactiveRoles.data.data.length,
      });
    } catch (error) {
      message.error("역할 통계 로드에 실패했습니다.");
    }
  };

  const handleCreate = () => {
    setEditingRole(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true });
    setIsModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingRole(record);
    form.setFieldsValue(record);
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await dispatch(deleteRole(id)).unwrap();
      message.success("역할이 삭제되었습니다.");
      loadRoleStats();
    } catch (error) {
      message.error(error || "역할 삭제에 실패했습니다.");
    }
  };

  const handleToggleActive = async (record) => {
    try {
      if (record.isActive) {
        await roleService.deactivateRole(record.id);
        message.success("역할이 비활성화되었습니다.");
      } else {
        await roleService.activateRole(record.id);
        message.success("역할이 활성화되었습니다.");
      }
      dispatch(fetchRoles());
      loadRoleStats();
    } catch (error) {
      message.error("역할 상태 변경에 실패했습니다.");
    }
  };

  const handleSubmit = async (values) => {
    try {
      if (editingRole) {
        await dispatch(
          updateRole({ id: editingRole.id, roleData: values })
        ).unwrap();
        message.success("역할이 수정되었습니다.");
      } else {
        await dispatch(createRole(values)).unwrap();
        message.success("역할이 생성되었습니다.");
      }
      setIsModalVisible(false);
      form.resetFields();
      loadRoleStats();
    } catch (error) {
      message.error(error || "역할 저장에 실패했습니다.");
    }
  };

  const columns = [
    {
      title: "ID",
      dataIndex: "id",
      key: "id",
      width: 80,
      align: "center",
    },
    {
      title: "역할명",
      dataIndex: "roleName",
      key: "roleName",
      width: 150,
      render: (text, record) => (
        <Space>
          <SafetyOutlined
            style={{ color: record.isActive ? "#52c41a" : "#d9d9d9" }}
          />
          <Text strong={record.isActive}>{text}</Text>
        </Space>
      ),
    },
    {
      title: "설명",
      dataIndex: "description",
      key: "description",
      width: 300,
      render: (text) => (
        <Text
          type="secondary"
          ellipsis={{ tooltip: text }}
          style={{ maxWidth: 280 }}
        >
          {text || "-"}
        </Text>
      ),
    },
    {
      title: "상태",
      dataIndex: "isActive",
      key: "isActive",
      width: 100,
      align: "center",
      render: (isActive) => (
        <Tag color={isActive ? "green" : "red"}>
          {isActive ? "활성" : "비활성"}
        </Tag>
      ),
    },
    {
      title: "생성일",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 200,
      render: (text) => (
        <Text type="secondary">
          {text ? new Date(text).toLocaleString("ko-KR") : "-"}
        </Text>
      ),
    },
    {
      title: "작업",
      key: "actions",
      width: 200,
      align: "center",
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="수정">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
              size="small"
            />
          </Tooltip>

          <Tooltip title={record.isActive ? "비활성화" : "활성화"}>
            <Button
              type="text"
              icon={
                record.isActive ? <StopOutlined /> : <CheckCircleOutlined />
              }
              onClick={() => handleToggleActive(record)}
              size="small"
              style={{
                color: record.isActive ? "#ff4d4f" : "#52c41a",
              }}
            />
          </Tooltip>

          <Tooltip title="삭제">
            <Popconfirm
              title="정말로 이 역할을 삭제하시겠습니까?"
              description="이 작업은 되돌릴 수 없습니다."
              onConfirm={() => handleDelete(record.id)}
              okText="삭제"
              cancelText="취소"
              okType="danger"
            >
              <Button
                type="text"
                icon={<DeleteOutlined />}
                size="small"
                style={{ color: "#ff4d4f" }}
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

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
          <SafetyOutlined /> 역할 관리
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          시스템 역할을 관리하고 권한을 설정할 수 있습니다.
        </p>
        <ErrorMessage
          error={error}
          onRetry={() => dispatch(fetchRoles())}
          showRetry={true}
          style={{ margin: "16px 0 0 0" }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>
          <Row gutter={16} style={{ marginBottom: "24px" }}>
            <Col span={8}>
              <SkeletonStatistic loading={isLoading && !roles.length}>
                <Statistic
                  title="전체 역할"
                  value={roleStats.total}
                  prefix={<TeamOutlined />}
                />
              </SkeletonStatistic>
            </Col>
            <Col span={8}>
              <SkeletonStatistic loading={isLoading && !roles.length}>
                <Statistic
                  title="활성 역할"
                  value={roleStats.active}
                  prefix={<CheckCircleOutlined />}
                  valueStyle={{ color: "#3f8600" }}
                />
              </SkeletonStatistic>
            </Col>
            <Col span={8}>
              <SkeletonStatistic loading={isLoading && !roles.length}>
                <Statistic
                  title="비활성 역할"
                  value={roleStats.inactive}
                  prefix={<StopOutlined />}
                  valueStyle={{ color: "#cf1322" }}
                />
              </SkeletonStatistic>
            </Col>
          </Row>

          <div
            style={{
              marginBottom: "16px",
              display: "flex",
              justifyContent: "space-between",
            }}
          >
            <div>
              <Text strong>역할 목록</Text>
            </div>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreate}
            >
              새 역할 추가
            </Button>
          </div>

          <Table
            columns={columns}
            dataSource={roles}
            rowKey="id"
            loading={isLoading}
            pagination={{
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) =>
                `${range[0]}-${range[1]} / 총 ${total}개`,
            }}
            scroll={{ x: 800 }}
          />
        </Card>
      </div>

      <Modal
        title={editingRole ? "역할 수정" : "새 역할 추가"}
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ isActive: true }}
        >
          <Form.Item
            name="roleName"
            label="역할명"
            rules={[
              { required: true, message: "역할명을 입력해주세요." },
              { min: 2, message: "역할명은 2자 이상이어야 합니다." },
              { max: 50, message: "역할명은 50자 이하여야 합니다." },
            ]}
          >
            <Input placeholder="역할명을 입력하세요" />
          </Form.Item>

          <Form.Item
            name="description"
            label="설명"
            rules={[{ max: 255, message: "설명은 255자 이하여야 합니다." }]}
          >
            <Input.TextArea
              placeholder="역할에 대한 설명을 입력하세요"
              rows={4}
            />
          </Form.Item>

          <Form.Item name="isActive" label="상태" valuePropName="checked">
            <Switch checkedChildren="활성" unCheckedChildren="비활성" />
          </Form.Item>

          <Divider />

          <Form.Item
            style={{
              marginBottom: 0,
              display: "flex",
              justifyContent: "flex-end",
            }}
          >
            <Space>
              <Button
                onClick={() => {
                  setIsModalVisible(false);
                  form.resetFields();
                }}
              >
                취소
              </Button>
              <Button type="primary" htmlType="submit" loading={isLoading}>
                {editingRole ? "수정" : "생성"}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RoleManagement;
