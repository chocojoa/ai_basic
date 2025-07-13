import { useState, useEffect } from "react";
import {
  Card,
  Form,
  Input,
  Button,
  Avatar,
  Row,
  Col,
  message,
  Modal,
  Space,
  Statistic,
} from "antd";
import {
  UserOutlined,
  ProfileOutlined,
  MailOutlined,
  PhoneOutlined,
  LockOutlined,
  EditOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
} from "@ant-design/icons";
import { useSelector, useDispatch } from "react-redux";
import authService from "../services/authService";
import ErrorMessage from "../components/common/ErrorMessage";
import {
  formatPhoneNumber,
  handlePhoneInput,
  validatePhoneNumber,
} from "../utils/phoneFormat";
import { getCurrentUser } from "../store/slices/authSlice";

const Profile = () => {
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [passwordModalVisible, setPasswordModalVisible] = useState(false);
  const [error, setError] = useState(null);
  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();

  // 컴포넌트 마운트 시 사용자 정보 새로 가져오기 (한 번만)
  useEffect(() => {
    if (!user) {
      dispatch(getCurrentUser());
    }
  }, [dispatch, user]);

  // 사용자 정보 변경 시 폼 값 업데이트
  useEffect(() => {
    if (user && profileModalVisible) {
      form.setFieldsValue({
        username: user.username,
        email: user.email,
        fullName: user.fullName,
        phone: user.phone || "",
      });
    }
  }, [user, form, profileModalVisible]);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // 전화번호 포맷팅 후 전송
      const formattedValues = {
        ...values,
        phone: values.phone ? formatPhoneNumber(values.phone) : "",
      };

      const response = await authService.updateProfile(formattedValues);

      // 사용자 정보 새로 가져오기
      await dispatch(getCurrentUser());

      message.success("프로필이 업데이트되었습니다");
      setProfileModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error(
        error.response?.data?.message || "프로필 업데이트에 실패했습니다"
      );
    } finally {
      setLoading(false);
    }
  };

  const onPasswordChange = async (values) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error("새 비밀번호가 일치하지 않습니다");
      return;
    }

    setLoading(true);
    try {
      await authService.changePassword(
        values.currentPassword,
        values.newPassword
      );
      message.success("비밀번호가 변경되었습니다");
      setPasswordModalVisible(false);
      passwordForm.resetFields();
    } catch (error) {
      message.error(
        error.response?.data?.message || "비밀번호 변경에 실패했습니다"
      );
    } finally {
      setLoading(false);
    }
  };

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
          <ProfileOutlined /> 내 정보
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          개인 정보를 확인하고 수정할 수 있습니다.
        </p>
        <ErrorMessage
          error={error}
          onRetry={() => {
            setError(null);
            dispatch(getCurrentUser());
          }}
          showRetry={true}
          style={{ margin: "16px 0 0 0" }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>
          <div style={{ textAlign: "center", marginBottom: 24 }}>
            <Avatar size={120} icon={<UserOutlined />} />
            <h2 style={{ marginTop: 16, marginBottom: 8 }}>
              {user?.fullName || user?.username}
            </h2>
            <p style={{ color: "#666" }}>
              {user?.roles?.map((role) => role.roleName).join(", ")}
            </p>
          </div>
          <Row
            gutter={[16, 16]}
            style={{ marginTop: "24px", marginBottom: "24px" }}
          >
            <Col xs={24} sm={12} md={6}>
              <Card
                style={{
                  boxShadow: "none",
                  border: "1px solid #f0f0f0",
                  borderRadius: "12px",
                  textAlign: "center",
                }}
              >
                <Statistic
                  title="이메일"
                  value={user?.email || "-"}
                  prefix={<MailOutlined />}
                  valueStyle={{ fontSize: "14px", color: "#722ed1" }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Card
                style={{
                  boxShadow: "none",
                  border: "1px solid #f0f0f0",
                  borderRadius: "12px",
                  textAlign: "center",
                }}
              >
                <Statistic
                  title="전화번호"
                  value={user?.phone ? formatPhoneNumber(user.phone) : "-"}
                  prefix={<PhoneOutlined />}
                  valueStyle={{ fontSize: "14px", color: "#fa8c16" }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Card
                style={{
                  boxShadow: "none",
                  border: "1px solid #f0f0f0",
                  borderRadius: "12px",
                  textAlign: "center",
                }}
              >
                <Statistic
                  title="가입일"
                  value={
                    user?.createdAt
                      ? new Date(user.createdAt).toLocaleDateString("ko-KR")
                      : "-"
                  }
                  prefix={<CalendarOutlined />}
                  valueStyle={{ fontSize: "14px", color: "#13c2c2" }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Card
                style={{
                  boxShadow: "none",
                  border: "1px solid #f0f0f0",
                  borderRadius: "12px",
                  textAlign: "center",
                }}
              >
                <Statistic
                  title="마지막 로그인"
                  value={
                    user?.lastLogin
                      ? new Date(user.lastLogin).toLocaleDateString("ko-KR")
                      : "-"
                  }
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ fontSize: "14px", color: "#eb2f96" }}
                />
              </Card>
            </Col>
          </Row>
          <div style={{ textAlign: "center" }}>
            <Space>
              <Button
                type="primary"
                icon={<EditOutlined />}
                onClick={() => setProfileModalVisible(true)}
              >
                프로필 수정
              </Button>
              <Button
                icon={<LockOutlined />}
                onClick={() => setPasswordModalVisible(true)}
              >
                비밀번호 변경
              </Button>
            </Space>
          </div>
        </Card>
      </div>

      {/* 프로필 수정 모달 */}
      <Modal
        title="프로필 수정"
        open={profileModalVisible}
        onCancel={() => {
          setProfileModalVisible(false);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{
            username: user?.username,
            email: user?.email,
            fullName: user?.fullName,
            phone: user?.phone,
          }}
        >
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="username"
                label="사용자명"
                rules={[
                  { required: true, message: "사용자명을 입력해주세요!" },
                ]}
              >
                <Input prefix={<UserOutlined />} disabled />
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

          <Form.Item>
            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading}>
                  변경
                </Button>
                <Button onClick={() => setProfileModalVisible(false)}>
                  취소
                </Button>
              </Space>
            </div>
          </Form.Item>
        </Form>
      </Modal>

      {/* 비밀번호 변경 모달 */}
      <Modal
        title="비밀번호 변경"
        open={passwordModalVisible}
        onCancel={() => {
          setPasswordModalVisible(false);
          passwordForm.resetFields();
        }}
        footer={null}
        width={500}
      >
        <Form form={passwordForm} layout="vertical" onFinish={onPasswordChange}>
          <Form.Item
            name="currentPassword"
            label="현재 비밀번호"
            rules={[
              { required: true, message: "현재 비밀번호를 입력해주세요!" },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="새 비밀번호"
            rules={[
              { required: true, message: "새 비밀번호를 입력해주세요!" },
              { min: 6, message: "비밀번호는 최소 6자 이상이어야 합니다!" },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="새 비밀번호 확인"
            rules={[
              { required: true, message: "새 비밀번호를 다시 입력해주세요!" },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} />
          </Form.Item>

          <Form.Item>
            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <Space>
                <Button type="primary" htmlType="submit" loading={loading}>
                  변경
                </Button>
                <Button onClick={() => setPasswordModalVisible(false)}>
                  취소
                </Button>
              </Space>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Profile;
