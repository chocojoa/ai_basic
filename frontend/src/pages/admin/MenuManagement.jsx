import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Tag,
  Card,
  message,
  Switch,
  Popconfirm,
  InputNumber,
  TreeSelect,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  MenuOutlined,
} from "@ant-design/icons";
import { useSelector, useDispatch } from "react-redux";
import {
  fetchMenus,
  createMenu,
  updateMenu,
  deleteMenu,
} from "../../store/slices/menuSlice";
import LoadingSpinner from "../../components/common/LoadingSpinner";
import ErrorMessage from "../../components/common/ErrorMessage";

const MenuManagement = () => {
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);
  const [editingMenu, setEditingMenu] = useState(null);
  const [error, setError] = useState(null);

  const dispatch = useDispatch();
  const { menus, isLoading } = useSelector((state) => state.menu);

  useEffect(() => {
    loadInitialData();
  }, [dispatch]);

  const loadInitialData = async () => {
    try {
      setError(null);
      await dispatch(fetchMenus()).unwrap();
    } catch (error) {
      setError(error);
    }
  };

  const handleAdd = () => {
    setEditingMenu(null);
    setModalVisible(true);
    form.resetFields();
  };

  const handleEdit = (record) => {
    setEditingMenu(record);
    setModalVisible(true);
    form.setFieldsValue(record);
  };

  const handleDelete = async (id) => {
    try {
      await dispatch(deleteMenu(id)).unwrap();
      message.success("메뉴가 삭제되었습니다");
    } catch (error) {
      message.error("메뉴 삭제에 실패했습니다");
    }
  };

  const handleSubmit = async (values) => {
    try {
      if (editingMenu) {
        await dispatch(
          updateMenu({ id: editingMenu.id, menuData: values })
        ).unwrap();
        message.success("메뉴가 수정되었습니다");
      } else {
        await dispatch(createMenu(values)).unwrap();
        message.success("메뉴가 생성되었습니다");
      }
      setModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error(
        editingMenu ? "메뉴 수정에 실패했습니다" : "메뉴 생성에 실패했습니다"
      );
    }
  };

  const handleReset = async () => {
    try {
      setError(null);
      searchForm.resetFields();
      await dispatch(fetchMenus()).unwrap();
    } catch (error) {
      setError(error);
      message.error("데이터 새로고침 중 오류가 발생했습니다");
    }
  };

  const handleRetry = () => {
    loadInitialData();
  };

  // 메뉴 트리 데이터 구성
  const buildMenuTree = (menus) => {
    const menuMap = {};
    const tree = [];

    menus.forEach((menu) => {
      menuMap[menu.id] = { ...menu, children: [] };
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

  const treeData = buildMenuTree(menus).map((menu) => ({
    title: menu.menuName,
    value: menu.id,
    children:
      menu.children?.map((child) => ({
        title: child.menuName,
        value: child.id,
      })) || [],
  }));

  const columns = [
    {
      title: "메뉴명",
      dataIndex: "menuName",
      key: "menuName",
      sorter: true,
    },
    {
      title: "상위메뉴",
      dataIndex: "parentId",
      key: "parentId",
      render: (parentId) => {
        const parent = menus.find((m) => m.id === parentId);
        return parent ? parent.menuName : "-";
      },
    },
    {
      title: "URL",
      dataIndex: "url",
      key: "url",
    },
    {
      title: "아이콘",
      dataIndex: "icon",
      key: "icon",
    },
    {
      title: "순서",
      dataIndex: "orderNum",
      key: "orderNum",
      sorter: true,
    },
    {
      title: "표시상태",
      dataIndex: "isVisible",
      key: "isVisible",
      render: (isVisible) => (
        <Tag color={isVisible ? "green" : "red"}>
          {isVisible ? "표시" : "숨김"}
        </Tag>
      ),
    },
    {
      title: "활성상태",
      dataIndex: "isActive",
      key: "isActive",
      render: (isActive) => (
        <Tag color={isActive ? "green" : "red"}>
          {isActive ? "활성" : "비활성"}
        </Tag>
      ),
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
            title="메뉴를 삭제하시겠습니까?"
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

  if (isLoading && !menus.length) {
    return <LoadingSpinner tip="메뉴 데이터를 불러오는 중..." />;
  }

  if (error && !menus.length) {
    return (
      <div>
        <h1>메뉴 관리</h1>
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
          <MenuOutlined /> 메뉴 관리
        </h1>
        <p style={{ color: "#666", margin: 0 }}>
          시스템 메뉴를 관리하고 계층 구조를 설정할 수 있습니다.
        </p>
        <ErrorMessage
          error={error}
          onRetry={handleRetry}
          showRetry={true}
          style={{ margin: "16px 0 0 0" }}
        />
      </div>

      <div style={{ padding: "0 12px 12px 12px" }}>
        {error && menus.length > 0 && (
          <ErrorMessage
            error={error}
            onRetry={handleRetry}
            type="warning"
            style={{ marginBottom: 16 }}
          />
        )}

        <Card style={{ boxShadow: "none", border: "1px solid #f0f0f0" }}>
          <div
            style={{
              marginBottom: "16px",
              display: "flex",
              justifyContent: "space-between",
            }}
          >
            <div>
              <span style={{ fontWeight: "bold" }}>메뉴 목록</span>
            </div>
            <div>
              <Button
                onClick={handleReset}
                icon={<ReloadOutlined />}
                style={{ marginRight: 8 }}
              >
                새로고침
              </Button>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={handleAdd}
              >
                메뉴 추가
              </Button>
            </div>
          </div>

          <Table
            columns={columns}
            dataSource={menus}
            rowKey="id"
            loading={isLoading}
            pagination={{
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total, range) =>
                `${range[0]}-${range[1]} / 총 ${total}개`,
            }}
          />
        </Card>
      </div>

      <Modal
        title={editingMenu ? "메뉴 수정" : "메뉴 추가"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="menuName"
            label="메뉴명"
            rules={[{ required: true, message: "메뉴명을 입력해주세요!" }]}
          >
            <Input prefix={<MenuOutlined />} />
          </Form.Item>

          <Form.Item name="parentId" label="상위메뉴">
            <TreeSelect
              showSearch
              style={{ width: "100%" }}
              value={null}
              popupStyle={{ maxHeight: 400, overflow: "auto" }}
              placeholder="상위메뉴 선택 (선택사항)"
              allowClear
              treeDefaultExpandAll
              treeData={treeData}
            />
          </Form.Item>

          <Form.Item name="url" label="URL">
            <Input placeholder="예: /admin/users" />
          </Form.Item>

          <Form.Item name="icon" label="아이콘">
            <Input placeholder="예: UserOutlined" />
          </Form.Item>

          <Form.Item
            name="orderNum"
            label="순서"
            rules={[{ required: true, message: "순서를 입력해주세요!" }]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          <Form.Item name="description" label="설명">
            <Input.TextArea rows={3} />
          </Form.Item>

          <Form.Item
            name="isVisible"
            label="표시상태"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch checkedChildren="표시" unCheckedChildren="숨김" />
          </Form.Item>

          <Form.Item
            name="isActive"
            label="활성상태"
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
                  {editingMenu ? "수정" : "생성"}
                </Button>
              </Space>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MenuManagement;
