import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Tag,
  Card,
  Row,
  Col,
  message,
  Switch,
  Popconfirm,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  LockOutlined,
} from "@ant-design/icons";
import { useSelector, useDispatch } from "react-redux";
import {
  fetchUsers,
  createUser,
  updateUser,
  deleteUser,
} from "../../store/slices/userSlice";
import { fetchRoles } from "../../store/slices/roleSlice";
import LoadingSpinner from "../../components/common/LoadingSpinner";
import ErrorMessage from "../../components/common/ErrorMessage";
import useResponsive from "../../hooks/useResponsive";
import {
  formatPhoneNumber,
  handlePhoneInput,
  validatePhoneNumber,
} from "../../utils/phoneFormat";
import userService from "../../services/userService";

const UserManagement = () => {
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [searchParams, setSearchParams] = useState({});
  const [error, setError] = useState(null);

  const dispatch = useDispatch();
  const { users, isLoading, pagination } = useSelector((state) => state.user);
  const { roles } = useSelector((state) => state.role);
  const { isMobile } = useResponsive();

  useEffect(() => {
    loadInitialData();
  }, [dispatch]);

  const loadInitialData = async () => {
    try {
      setError(null);
      await Promise.all([
        dispatch(fetchUsers()).unwrap(),
        dispatch(fetchRoles()).unwrap(),
      ]);
    } catch (error) {
      setError(error);
    }
  };

  const handleAdd = () => {
    setEditingUser(null);
    setModalVisible(true);
    form.resetFields();
  };

  const handleEdit = (record) => {
    setEditingUser(record);
    setModalVisible(true);
    form.setFieldsValue({
      ...record,
      phone: record.phone ? formatPhoneNumber(record.phone) : "",
      roleIds: record.roles?.map((role) => role.id) || [],
    });
  };

  const handleDelete = async (id) => {
    try {
      await dispatch(deleteUser(id)).unwrap();
      message.success("사용자가 삭제되었습니다");
    } catch (error) {
      message.error("사용자 삭제에 실패했습니다");
    }
  };

  const handleResetPassword = async (user) => {
    try {
      await userService.adminResetPassword(user.id);

      message.success(
        `${
          user.fullName || user.username
        }의 비밀번호가 초기화되었습니다. 기본 비밀번호: ${user.username}123`
      );
    } catch (error) {
      message.error(
        error.response?.data?.message || "비밀번호 초기화에 실패했습니다"
      );
    }
  };

  const handleSubmit = async (values) => {
    try {
      // 전화번호 포맷팅 후 전송
      const formattedValues = {
        ...values,
        phone: values.phone ? formatPhoneNumber(values.phone) : "",
      };

      if (editingUser) {
        await dispatch(
          updateUser({ id: editingUser.id, userData: formattedValues })
        ).unwrap();
        message.success("사용자가 수정되었습니다");
      } else {
        await dispatch(createUser(formattedValues)).unwrap();
        message.success("사용자가 생성되었습니다");
      }
      setModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error(
        editingUser
          ? "사용자 수정에 실패했습니다"
          : "사용자 생성에 실패했습니다"
      );
    }
  };

  const handleSearch = async (values) => {
    try {
      setError(null);
      setSearchParams(values);
      await dispatch(fetchUsers(values)).unwrap();
    } catch (error) {
      setError(error);
      message.error("검색 중 오류가 발생했습니다");
    }
  };

  const handleReset = async () => {
    try {
      setError(null);
      searchForm.resetFields();
      setSearchParams({});
      await dispatch(fetchUsers()).unwrap();
    } catch (error) {
      setError(error);
      message.error("데이터 새로고침 중 오류가 발생했습니다");
    }
  };

  const handleRetry = () => {
    loadInitialData();
  };

  const columns = [
    {
      title: "사용자명",
      dataIndex: "username",
      key: "username",
      sorter: true,
    },
    {
      title: "이름",
      dataIndex: "fullName",
      key: "fullName",
      sorter: true,
    },
    {
      title: "이메일",
      dataIndex: "email",
      key: "email",
      sorter: true,
    },
    {
      title: "전화번호",
      dataIndex: "phone",
      key: "phone",
      render: (phone) => (phone ? formatPhoneNumber(phone) : "-"),
    },
    {
      title: "역할",
      dataIndex: "roles",
      key: "roles",
      render: (roles) => (
        <>
          {roles?.map((role) => (
            <Tag key={role.id} color="blue">
              {role.roleName}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: "상태",
      dataIndex: "isActive",
      key: "isActive",
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
      width: 180,
      sorter: true,
      render: (text) => (text ? new Date(text).toLocaleString("ko-KR") : "-"),
    },
    {
      title: "작업",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            수정
          </Button>
          <Popconfirm
            title="비밀번호를 초기화하시겠습니까?"
            description={`기본 비밀번호: ${record.username}123`}
            onConfirm={async () => {
              await handleResetPassword(record);
            }}
            okText="초기화"
            cancelText="취소"
            placement="topRight"
          >
            <Button type="link" icon={<LockOutlined />}>
              비밀번호 초기화
            </Button>
          </Popconfirm>
          <Popconfirm
            title="사용자를 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.id)}
            okText="삭제"
            cancelText="취소"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              삭제
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  if (isLoading && !users.length) {
    return <LoadingSpinner tip="사용자 데이터를 불러오는 중..." />;
  }

  if (error && !users.length) {
    return (
      <div>
        <h1>회원 관리</h1>
        <ErrorMessage error={error} onRetry={handleRetry} showRetry={true} />
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
          <UserOutlined /> 회원 관리
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          시스템 사용자를 관리하고 권한을 설정할 수 있습니다.
        </p>
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        {error && users.length > 0 && (
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
          <Form form={searchForm} layout="vertical" onFinish={handleSearch}>
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={12} md={8}>
                <Form.Item name="username" label="사용자명">
                  <Input placeholder="사용자명 검색" />
                </Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Form.Item name="email" label="이메일">
                  <Input placeholder="이메일 검색" />
                </Form.Item>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Form.Item name="isActive" label="상태">
                  <Select placeholder="상태 선택" allowClear>
                    <Select.Option value={true}>활성</Select.Option>
                    <Select.Option value={false}>비활성</Select.Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>
            <Row>
              <Col xs={24}>
                <Form.Item
                  style={{
                    marginBottom: 0,
                    display: "flex",
                    justifyContent: "flex-end",
                  }}
                >
                  <Space wrap>
                    <Button
                      type="primary"
                      htmlType="submit"
                      icon={<SearchOutlined />}
                    >
                      검색
                    </Button>
                    <Button onClick={handleReset} icon={<ReloadOutlined />}>
                      초기화
                    </Button>
                  </Space>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </Card>

        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>
          <div
            style={{
              marginBottom: "16px",
              display: "flex",
              justifyContent: "space-between",
            }}
          >
            <div>
              <span style={{ fontWeight: "bold" }}>사용자 목록</span>
            </div>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              사용자 추가
            </Button>
          </div>

          <Table
            columns={columns}
            dataSource={users}
            rowKey="id"
            loading={isLoading}
            scroll={{ x: 800 }}
            pagination={{
              current: pagination.current,
              pageSize: pagination.pageSize,
              total: pagination.total,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) =>
                `${range[0]}-${range[1]} / 총 ${total}개`,
              responsive: true,
            }}
          />
        </Card>
      </div>

      <Modal
        title={editingUser ? "사용자 수정" : "사용자 추가"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={isMobile ? "95%" : 600}
        centered={isMobile}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="username"
                label="사용자명"
                rules={[
                  { required: true, message: "사용자명을 입력해주세요!" },
                ]}
              >
                <Input prefix={<UserOutlined />} disabled={!!editingUser} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="email"
                label="이메일"
                rules={[
                  { required: true, message: "이메일을 입력해주세요!" },
                  { type: "email", message: "올바른 이메일 형식이 아닙니다!" },
                ]}
              >
                <Input prefix={<MailOutlined />} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="fullName"
                label="이름"
                rules={[{ required: true, message: "이름을 입력해주세요!" }]}
              >
                <Input prefix={<UserOutlined />} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="phone"
                label="전화번호"
                rules={[
                  {
                    validator: (_, value) => {
                      if (!value || validatePhoneNumber(value)) {
                        return Promise.resolve();
                      }
                      return Promise.reject(
                        new Error("올바른 전화번호 형식이 아닙니다")
                      );
                    },
                  },
                ]}
              >
                <Input
                  prefix={<PhoneOutlined />}
                  placeholder="010-1234-5678"
                  maxLength={13}
                  onChange={(e) => {
                    const formatted = handlePhoneInput(e.target.value);
                    form.setFieldValue("phone", formatted);
                  }}
                />
              </Form.Item>
            </Col>
          </Row>

          {!editingUser && (
            <Form.Item
              name="password"
              label="비밀번호"
              rules={[
                { required: true, message: "비밀번호를 입력해주세요!" },
                { min: 6, message: "비밀번호는 최소 6자 이상이어야 합니다!" },
              ]}
            >
              <Input.Password />
            </Form.Item>
          )}

          <Form.Item
            name="roleIds"
            label="역할"
            rules={[{ required: true, message: "역할을 선택해주세요!" }]}
          >
            <Select mode="multiple" placeholder="역할 선택">
              {roles.map((role) => (
                <Select.Option key={role.id} value={role.id}>
                  {role.roleName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="isActive"
            label="상태"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="활성" unCheckedChildren="비활성" />
          </Form.Item>

          <Form.Item>
            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <Space>
                <Button onClick={() => setModalVisible(false)}>취소</Button>
                <Button type="primary" htmlType="submit" loading={isLoading}>
                  {editingUser ? "수정" : "생성"}
                </Button>
              </Space>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement;
