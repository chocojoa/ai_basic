import { useEffect, useState } from "react";
import { Form, Input, Button, Card, message, Modal } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { login, clearError } from "../../store/slices/authSlice";
import authService from "../../services/authService";

const Login = () => {
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [forceChangeModalVisible, setForceChangeModalVisible] = useState(false);
  const [isForceChanging, setIsForceChanging] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { isLoading, error, isAuthenticated, isInitialized, user } =
    useSelector((state) => state.auth);

  useEffect(() => {
    // 초기화가 완료되고 인증된 경우 비밀번호 변경 필요 여부 확인
    if (isInitialized && isAuthenticated && user) {
      if (user.passwordChangeRequired) {
        setForceChangeModalVisible(true);
      } else {
        navigate("/dashboard");
      }
    }
  }, [isAuthenticated, isInitialized, user, navigate]);

  useEffect(() => {
    if (error) {
      message.error(error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const onFinish = async (values) => {
    try {
      await dispatch(login(values)).unwrap();
      message.success("로그인 성공!");
      // navigate는 useEffect에서 isAuthenticated 변화를 감지하여 처리
    } catch (error) {
      // 에러는 useEffect에서 처리
    }
  };

  const handleForcePasswordChange = async (values) => {
    setIsForceChanging(true);
    try {
      await authService.forceChangePassword(
        values.newPassword,
        values.confirmPassword
      );
      message.success("비밀번호가 변경되었습니다. 다시 로그인해주세요.");
      setForceChangeModalVisible(false);
      // 로그아웃 후 다시 로그인하도록 유도
      localStorage.removeItem("token");
      localStorage.removeItem("refreshToken");
      window.location.reload();
    } catch (error) {
      message.error(
        error.response?.data?.message || "비밀번호 변경에 실패했습니다."
      );
    } finally {
      setIsForceChanging(false);
    }
  };

  return (
    <div className="login-container">
      <Card className="login-form">
        <div className="login-title">기초 프로젝트</div>
        <Form form={form} name="login" onFinish={onFinish} autoComplete="off">
          <Form.Item
            name="username"
            rules={[
              { required: true, message: "사용자명을 입력해주세요!" },
              { min: 3, message: "사용자명은 최소 3자 이상이어야 합니다!" },
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="사용자명"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: "비밀번호를 입력해주세요!" },
              { min: 6, message: "비밀번호는 최소 6자 이상이어야 합니다!" },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="비밀번호"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={isLoading}
              size="large"
              block
            >
              로그인
            </Button>
          </Form.Item>
        </Form>

        <div
          style={{
            marginTop: 16,
            fontSize: 12,
            color: "#666",
            textAlign: "center",
          }}
        >
          <div>테스트 계정:</div>
          <div>관리자: admin / admin123</div>
          <div>일반사용자: user / user123</div>
        </div>
      </Card>

      {/* 강제 비밀번호 변경 모달 */}
      <Modal
        title="비밀번호 변경 필요"
        open={forceChangeModalVisible}
        closable={false}
        maskClosable={false}
        footer={null}
        width={500}
      >
        <div style={{ marginBottom: 16, color: "#ff4d4f" }}>
          <strong>보안을 위해 비밀번호를 변경해야 합니다.</strong>
        </div>
        <div style={{ marginBottom: 24, color: "#666" }}>
          관리자에 의해 비밀번호가 변경되었습니다. 새로운 비밀번호를
          설정해주세요.
        </div>

        <Form
          form={passwordForm}
          layout="vertical"
          onFinish={handleForcePasswordChange}
        >
          <Form.Item
            name="newPassword"
            label="새 비밀번호"
            rules={[
              { required: true, message: "새 비밀번호를 입력해주세요!" },
              { min: 6, message: "비밀번호는 최소 6자 이상이어야 합니다!" },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="새 비밀번호"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="새 비밀번호 확인"
            dependencies={["newPassword"]}
            rules={[
              { required: true, message: "새 비밀번호를 다시 입력해주세요!" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("newPassword") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error("새 비밀번호가 일치하지 않습니다!")
                  );
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="새 비밀번호 확인"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={isForceChanging}
              block
              size="large"
            >
              비밀번호 변경
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Login;
