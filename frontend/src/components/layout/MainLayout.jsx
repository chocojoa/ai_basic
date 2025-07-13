import React, { useState, useEffect } from "react";
import {
  Layout,
  Menu,
  Button,
  Dropdown,
  Avatar,
  Space,
  message,
  Drawer,
} from "antd";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import {
  DashboardOutlined,
  UserOutlined,
  SettingOutlined,
  MenuOutlined,
  LogoutOutlined,
  ProfileOutlined,
  SafetyOutlined,
  LockOutlined,
  FileTextOutlined,
  TeamOutlined,
} from "@ant-design/icons";
import { logout } from "../../store/slices/authSlice";
import { fetchUserMenus, clearUserMenus } from "../../store/slices/menuSlice";
import useResponsive from "../../hooks/useResponsive";

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { isMobile } = useResponsive();

  const { user } = useSelector((state) => state.auth);
  const { userMenus, isLoading } = useSelector((state) => state.menu);

  useEffect(() => {
    if (user) {
      dispatch(fetchUserMenus());
    }
  }, [dispatch, user]);

  // 모바일에서는 collapsed 상태 해제
  useEffect(() => {
    if (isMobile) {
      setCollapsed(false);
    }
  }, [isMobile]);

  const handleLogout = async () => {
    try {
      await dispatch(logout()).unwrap();
      dispatch(clearUserMenus()); // 사용자 메뉴 정리
      message.success("로그아웃되었습니다");
      navigate("/login");
    } catch (error) {
      message.error("로그아웃 중 오류가 발생했습니다");
    }
  };

  // 아이콘 매핑
  const getMenuIcon = (iconName) => {
    const iconMap = {
      DashboardOutlined: <DashboardOutlined />,
      UserOutlined: <UserOutlined />,
      SettingOutlined: <SettingOutlined />,
      MenuOutlined: <MenuOutlined />,
      SafetyOutlined: <SafetyOutlined />,
      TeamOutlined: <TeamOutlined />,
      LockOutlined: <LockOutlined />,
      ProfileOutlined: <ProfileOutlined />,
      FileTextOutlined: <FileTextOutlined />,
    };
    return iconMap[iconName] || <MenuOutlined />;
  };

  // 서버에서 받은 메뉴 데이터를 Ant Design Menu 형태로 변환
  const buildMenuItems = (menus) => {
    if (!menus || menus.length === 0) return [];

    // 서버에서 이미 트리 구조로 정리된 메뉴를 Ant Design Menu 형태로 변환
    const convertToAntdMenu = (menuItems) => {
      return menuItems.map((menu) => {
        const antdMenuItem = {
          key: menu.url || `menu-${menu.id}`,
          icon: getMenuIcon(menu.icon),
          label: menu.menuName,
        };

        // 하위 메뉴가 있는 경우 재귀적으로 처리
        if (menu.children && menu.children.length > 0) {
          antdMenuItem.children = convertToAntdMenu(menu.children);
        }

        return antdMenuItem;
      });
    };

    const result = convertToAntdMenu(menus);
    return result;
  };

  const userMenuItems = [
    {
      key: "profile",
      icon: <ProfileOutlined />,
      label: "내 정보",
      onClick: () => navigate("/profile"),
    },
    {
      type: "divider",
    },
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "로그아웃",
      onClick: handleLogout,
    },
  ];

  const menuItems = buildMenuItems(userMenus);

  // 기본적으로 첫 번째 부모 메뉴를 열도록 설정
  const defaultOpenKeys = menuItems
    .filter((item) => item.children && item.children.length > 0)
    .map((item) => item.key)
    .slice(0, 1); // 첫 번째 부모 메뉴만 열기

  const onMenuClick = ({ key }) => {
    // URL이 있는 메뉴만 네비게이션
    if (key && key.startsWith("/")) {
      navigate(key);
      // 모바일에서 메뉴 클릭 시 드로어 닫기
      if (isMobile) {
        setMobileDrawerOpen(false);
      }
    }
  };

  const toggleMobileDrawer = () => {
    setMobileDrawerOpen(!mobileDrawerOpen);
  };

  // 사이드바 메뉴 컴포넌트
  const SidebarMenu = () => (
    <>
      <div
        className="logo"
        style={{
          height: 64,
          padding: "16px",
          color: "#1890ff",
          fontSize: isMobile ? "16px" : "18px",
          fontWeight: "bold",
          textAlign: "center",
          borderBottom: "1px solid #f0f0f0",
        }}
      >
        {collapsed && !isMobile ? "BP" : "기초 프로젝트"}
      </div>
      {isLoading ? (
        <div style={{ padding: "16px", textAlign: "center", color: "#999999" }}>
          메뉴 로딩 중...
        </div>
      ) : (
        <Menu
          theme="light"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={defaultOpenKeys}
          items={menuItems}
          onClick={onMenuClick}
          style={{ border: "none" }}
        />
      )}
    </>
  );

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* 데스크톱 사이드바 */}
      {!isMobile && (
        <Sider trigger={null} collapsible collapsed={collapsed}>
          <SidebarMenu />
        </Sider>
      )}

      {/* 모바일 드로어 */}
      {isMobile && (
        <Drawer
          title={null}
          placement="left"
          onClose={() => setMobileDrawerOpen(false)}
          open={mobileDrawerOpen}
          bodyStyle={{ padding: 0, backgroundColor: "#ffffff" }}
          width={250}
        >
          <SidebarMenu />
        </Drawer>
      )}

      <Layout>
        <Header
          style={{
            padding: 0,
            background: "#fff",
            borderBottom: "1px solid #f0f0f0",
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              height: "100%",
              paddingRight: isMobile ? "16px" : "24px",
            }}
          >
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={
                isMobile ? toggleMobileDrawer : () => setCollapsed(!collapsed)
              }
              style={{
                fontSize: "16px",
                width: 32,
                height: 32,
                marginLeft: 16,
              }}
            />
            <Space size={isMobile ? "small" : "middle"}>
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space style={{ cursor: "pointer" }}>
                  <Avatar
                    size={isMobile ? "small" : "default"}
                    icon={<UserOutlined />}
                  />
                  {!isMobile && <span>{user?.fullName || user?.username}</span>}
                </Space>
              </Dropdown>
            </Space>
          </div>
        </Header>
        <Content
          style={{
            margin: isMobile ? "12px 12px" : "16px 16px",
            padding: isMobile ? 8 : 12,
            minHeight: 280,
            overflow: "initial",
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
