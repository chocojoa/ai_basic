import React, { useState } from "react";
import { Form, Input, Button, Card, Typography, message, Row, Col } from "antd";
import {
  UserOutlined,
  MailOutlined,
  LockOutlined,
  UserAddOutlined,
  PhoneOutlined,
} from "@ant-design/icons";
import { Link, useNavigate } from "react-router-dom";
import { authService } from "../../services/authService";

const { Title, Text } = Typography;

const Register = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      await authService.register(values);
      message.success("회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.");
      navigate("/login");
    } catch (error) {
      message.error(
        error.response?.data?.message || "회원가입 중 오류가 발생했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  const validateConfirmPassword = (_, value) => {
    if (!value || form.getFieldValue("password") === value) {
      return Promise.resolve();
    }
    return Promise.reject(new Error("비밀번호가 일치하지 않습니다."));
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
      }}
    >
      <Card
        style={{
          width: 500,
          boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
          borderRadius: 8,
        }}
      >
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <UserAddOutlined
            style={{ fontSize: 48, color: "#1890ff", marginBottom: 16 }}
          />
          <Title level={2} style={{ marginBottom: 8 }}>
            회원가입
          </Title>
          <Text type="secondary">새 계정을 만들어 시작하세요</Text>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          layout="vertical"
          requiredMark={false}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="username"
                label="사용자명"
                rules={[
                  { required: true, message: "사용자명을 입력해주세요." },
                  { min: 3, message: "사용자명은 3자 이상이어야 합니다." },
                  { max: 50, message: "사용자명은 50자 이하여야 합니다." },
                  {
                    pattern: /^[a-zA-Z0-9_]+$/,
                    message:
                      "사용자명은 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.",
                  },
                ]}
              >
                <Input
                  prefix={<UserOutlined />}
                  placeholder="사용자명"
                  size="large"
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="fullName"
                label="이름"
                rules={[
                  { required: true, message: "이름을 입력해주세요." },
                  { min: 2, message: "이름은 2자 이상이어야 합니다." },
                  { max: 100, message: "이름은 100자 이하여야 합니다." },
                ]}
              >
                <Input
                  prefix={<UserOutlined />}
                  placeholder="이름"
                  size="large"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="email"
            label="이메일"
            rules={[
              { required: true, message: "이메일을 입력해주세요." },
              { type: "email", message: "올바른 이메일 형식이 아닙니다." },
            ]}
          >
            <Input
              prefix={<MailOutlined />}
              placeholder="이메일"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="phone"
            label="전화번호"
            rules={[
              {
                pattern: /^[0-9\-\s]+$/,
                message:
                  "전화번호는 숫자, 하이픈(-), 공백만 사용할 수 있습니다.",
              },
            ]}
          >
            <Input
              prefix={<PhoneOutlined />}
              placeholder="전화번호 (선택사항)"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="비밀번호"
            rules={[
              { required: true, message: "비밀번호를 입력해주세요." },
              { min: 6, message: "비밀번호는 6자 이상이어야 합니다." },
              { max: 100, message: "비밀번호는 100자 이하여야 합니다." },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="비밀번호"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="비밀번호 확인"
            dependencies={["password"]}
            rules={[
              { required: true, message: "비밀번호 확인을 입력해주세요." },
              { validator: validateConfirmPassword },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="비밀번호 확인"
              size="large"
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              size="large"
              style={{ marginBottom: 16 }}
            >
              회원가입
            </Button>

            <div style={{ textAlign: "center" }}>
              <Text type="secondary">
                이미 계정이 있으신가요? <Link to="/login">로그인</Link>
              </Text>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Register;
