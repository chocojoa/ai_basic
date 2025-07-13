import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Modal,
  Space,
  message,
  Card,
  Typography,
  Tag,
  Tooltip,
  Row,
  Col,
  Statistic,
  Checkbox,
} from "antd";
import {
  SafetyOutlined,
  EditOutlined,
  MenuOutlined,
  UserOutlined,
  LockOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import { useDispatch, useSelector } from "react-redux";
import { fetchRoles } from "../../store/slices/roleSlice";
import { fetchMenus } from "../../store/slices/menuSlice";
import { refreshToken } from "../../store/slices/authSlice";
import permissionService from "../../services/permissionService";
import ErrorMessage from "../../components/common/ErrorMessage";

const { Title, Text } = Typography;

const PermissionManagement = () => {
  const dispatch = useDispatch();
  const { roles, isLoading: rolesLoading } = useSelector((state) => state.role);
  const { menus, isLoading: menusLoading } = useSelector((state) => state.menu);

  const [isModalVisible, setIsModalVisible] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
  const [selectedRolePermissions, setSelectedRolePermissions] = useState({});
  const [permissionStats, setPermissionStats] = useState({
    total: 0,
    configured: 0,
    roles: 0,
    menus: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    dispatch(fetchRoles());
    dispatch(fetchMenus());
    loadPermissionStats();
  }, [dispatch]);

  const loadPermissionStats = async () => {
    try {
      const [permissionCount, allPermissions] = await Promise.all([
        permissionService.getPermissionCount(),
        permissionService.getAllPermissions(),
      ]);

      const configuredRoles = new Set();
      const configuredMenus = new Set();

      allPermissions.data.data.forEach((permission) => {
        configuredRoles.add(permission.roleId);
        configuredMenus.add(permission.menuId);
      });

      setPermissionStats({
        total: permissionCount.data.data,
        configured: allPermissions.data.data.length,
        roles: configuredRoles.size,
        menus: configuredMenus.size,
      });
    } catch (error) {
      message.error("권한 통계 로드에 실패했습니다.");
    }
  };

  const handleEdit = async (record) => {
    setSelectedRole(record);
    setLoading(true);
    try {
      const response =
        await permissionService.getPermissionsWithMenuDetailsByRoleId(
          record.id
        );
      const rolePermissions = {};

      // 응답 데이터가 있는 경우만 처리
      if (response.data && response.data.data) {
        response.data.data.forEach((permission) => {
          rolePermissions[permission.menuId] = {
            canRead: permission.canRead,
            canWrite: permission.canWrite,
            canDelete: permission.canDelete,
          };
        });
      }

      setSelectedRolePermissions(rolePermissions);
      setIsModalVisible(true);
    } catch (error) {
      if (error.response?.status === 401) {
        message.error("인증이 만료되었습니다. 다시 로그인해주세요.");
        // 토큰 재발급 시도
        try {
          await dispatch(refreshToken());
          // 재시도
          const retryResponse =
            await permissionService.getPermissionsWithMenuDetailsByRoleId(
              record.id
            );
          const rolePermissions = {};

          if (retryResponse.data && retryResponse.data.data) {
            retryResponse.data.data.forEach((permission) => {
              rolePermissions[permission.menuId] = {
                canRead: permission.canRead,
                canWrite: permission.canWrite,
                canDelete: permission.canDelete,
              };
            });
          }

          setSelectedRolePermissions(rolePermissions);
          setIsModalVisible(true);
        } catch (refreshError) {
          message.error("토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
        }
      } else {
        message.error("권한 정보를 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!selectedRole) return;

    setLoading(true);
    try {
      const permissionsToUpdate = [];

      Object.keys(selectedRolePermissions).forEach((menuId) => {
        const permission = selectedRolePermissions[menuId];
        permissionsToUpdate.push({
          roleId: selectedRole.id,
          menuId: parseInt(menuId),
          canRead: permission.canRead || false,
          canWrite: permission.canWrite || false,
          canDelete: permission.canDelete || false,
        });
      });

      await permissionService.batchUpdatePermissionsByRoleId(
        selectedRole.id,
        permissionsToUpdate
      );
      message.success("권한이 수정되었습니다.");
      setIsModalVisible(false);
      loadPermissionStats();
    } catch (error) {
      message.error("권한 수정에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const buildMenuTree = (menus) => {
    const menuMap = {};
    const tree = [];

    menus.forEach((menu) => {
      menuMap[menu.id] = {
        ...menu,
        children: [],
        key: menu.id,
        title: menu.menuName,
      };
    });

    menus.forEach((menu) => {
      if (menu.parentId && menuMap[menu.parentId]) {
        menuMap[menu.parentId].children.push(menuMap[menu.id]);
      } else {
        tree.push(menuMap[menu.id]);
      }
    });

    return tree;
  };

  const treeData = buildMenuTree(menus);

  const handlePermissionChange = (menuId, permissionType, checked) => {
    setSelectedRolePermissions((prev) => ({
      ...prev,
      [menuId]: {
        ...prev[menuId],
        [permissionType]: checked,
      },
    }));
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
          {text ? new Date(text).toLocaleString('ko-KR') : "-"}
        </Text>
      ),
    },
    {
      title: "작업",
      key: "actions",
      width: 150,
      align: "center",
      render: (_, record) => (
        <Tooltip title="권한 설정">
          <Button
            type="primary"
            icon={<LockOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
            loading={loading && selectedRole?.id === record.id}
          >
            권한 설정
          </Button>
        </Tooltip>
      ),
    },
  ];

  const renderTreeNode = (node) => (
    <div
      key={node.key}
      style={{
        marginBottom: 12,
        padding: "8px",
        border: "1px solid #f0f0f0",
        borderRadius: "6px",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", marginBottom: 8 }}>
        <MenuOutlined style={{ marginRight: 8, color: "#1890ff" }} />
        <Text strong>{node.title}</Text>
        {node.url && (
          <Tag size="small" style={{ marginLeft: 8 }}>
            {node.url}
          </Tag>
        )}
      </div>
      <div style={{ marginLeft: 24 }}>
        <Space size="large">
          <Checkbox
            checked={selectedRolePermissions[node.key]?.canRead || false}
            onChange={(e) =>
              handlePermissionChange(node.key, "canRead", e.target.checked)
            }
          >
            <Space>
              <CheckCircleOutlined style={{ color: "#52c41a" }} />
              읽기
            </Space>
          </Checkbox>
          <Checkbox
            checked={selectedRolePermissions[node.key]?.canWrite || false}
            onChange={(e) =>
              handlePermissionChange(node.key, "canWrite", e.target.checked)
            }
          >
            <Space>
              <EditOutlined style={{ color: "#faad14" }} />
              쓰기
            </Space>
          </Checkbox>
          <Checkbox
            checked={selectedRolePermissions[node.key]?.canDelete || false}
            onChange={(e) =>
              handlePermissionChange(node.key, "canDelete", e.target.checked)
            }
          >
            <Space>
              <CloseCircleOutlined style={{ color: "#ff4d4f" }} />
              삭제
            </Space>
          </Checkbox>
        </Space>
      </div>
      {node.children && node.children.length > 0 && (
        <div style={{ marginLeft: 24, marginTop: 12 }}>
          {node.children.map((child) => renderTreeNode(child))}
        </div>
      )}
    </div>
  );

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
          <LockOutlined /> 권한 관리
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          역할별 메뉴 접근 권한을 설정하고 관리할 수 있습니다.
        </p>
        <ErrorMessage 
          error={error} 
          onRetry={() => {
            dispatch(fetchRoles());
            dispatch(fetchMenus());
            setError(null);
          }}
          showRetry={true}
          style={{ margin: '16px 0 0 0' }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        <Row gutter={16} style={{ marginBottom: "24px" }}>
          <Col span={8}>
            <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0", borderRadius: "12px" }}>
              <Statistic
                title="전체 권한"
                value={permissionStats.total}
                prefix={<LockOutlined />}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0", borderRadius: "12px" }}>
              <Statistic
                title="설정된 권한"
                value={permissionStats.configured}
                prefix={<CheckCircleOutlined />}
                valueStyle={{ color: "#3f8600" }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0", borderRadius: "12px" }}>
              <Statistic
                title="활성 역할"
                value={permissionStats.roles}
                prefix={<UserOutlined />}
                valueStyle={{ color: "#1890ff" }}
              />
            </Card>
          </Col>
        </Row>

        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>

          <div style={{ marginBottom: "16px" }}>
            <Text strong>역할별 권한 설정</Text>
          </div>

          <Table
            columns={columns}
            dataSource={roles}
            rowKey="id"
            loading={rolesLoading}
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
        title={
          <Space>
            <LockOutlined />
            권한 설정 - {selectedRole?.roleName}
          </Space>
        }
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setIsModalVisible(false)}>
            취소
          </Button>,
          <Button key="submit" type="primary" loading={loading} onClick={handleSubmit}>
            저장
          </Button>
        ]}
        width={900}
        confirmLoading={loading}
      >
        <div style={{ marginBottom: 16 }}>
          <Space>
            <Tag color="blue" icon={<SafetyOutlined />}>
              {selectedRole?.roleName}
            </Tag>
            <Text type="secondary">{selectedRole?.description}</Text>
          </Space>
        </div>

        <Card title="메뉴 권한 설정" size="small">
          <div style={{ maxHeight: 500, overflowY: "auto" }}>
            {treeData.map((node) => renderTreeNode(node))}
          </div>
        </Card>

        <div
          style={{
            marginTop: 16,
            padding: 16,
            backgroundColor: "#f6ffed",
            border: "1px solid #b7eb8f",
            borderRadius: 6,
          }}
        >
          <Title level={5} style={{ color: "#389e0d", marginBottom: 8 }}>
            <CheckCircleOutlined /> 권한 설명
          </Title>
          <Row gutter={16}>
            <Col span={8}>
              <Space>
                <CheckCircleOutlined style={{ color: "#52c41a" }} />
                <Text strong>읽기:</Text>
              </Space>
              <div style={{ marginLeft: 20, marginTop: 4 }}>
                <Text type="secondary">메뉴 접근 및 데이터 조회</Text>
              </div>
            </Col>
            <Col span={8}>
              <Space>
                <EditOutlined style={{ color: "#faad14" }} />
                <Text strong>쓰기:</Text>
              </Space>
              <div style={{ marginLeft: 20, marginTop: 4 }}>
                <Text type="secondary">데이터 생성 및 수정</Text>
              </div>
            </Col>
            <Col span={8}>
              <Space>
                <CloseCircleOutlined style={{ color: "#ff4d4f" }} />
                <Text strong>삭제:</Text>
              </Space>
              <div style={{ marginLeft: 20, marginTop: 4 }}>
                <Text type="secondary">데이터 삭제</Text>
              </div>
            </Col>
          </Row>
        </div>
      </Modal>
    </div>
  );
};

export default PermissionManagement;
