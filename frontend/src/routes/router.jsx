import { createBrowserRouter, Navigate } from "react-router-dom";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import Dashboard from "../pages/Dashboard";
import UserManagement from "../pages/admin/UserManagement";
import RoleManagement from "../pages/admin/RoleManagement";
import MenuManagement from "../pages/admin/MenuManagement";
import PermissionManagement from "../pages/admin/PermissionManagement";
import LogManagement from "../pages/admin/LogManagement";
import SystemMonitoring from "../pages/SystemMonitoring";
import Profile from "../pages/Profile";
import MainLayout from "../components/layout/MainLayout";
import ProtectedRoute from "../components/ProtectedRoute";

const router = createBrowserRouter([
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "/register",
    element: <Register />,
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: "dashboard",
        element: <Dashboard />,
      },
      {
        path: "profile",
        element: <Profile />,
      },
      {
        path: "admin/users",
        element: <UserManagement />,
      },
      {
        path: "admin/roles",
        element: <RoleManagement />,
      },
      {
        path: "admin/menus",
        element: <MenuManagement />,
      },
      {
        path: "admin/permissions",
        element: <PermissionManagement />,
      },
      {
        path: "admin/logs",
        element: <LogManagement />,
      },
      {
        path: "admin/monitoring",
        element: <SystemMonitoring />,
      },
    ],
  },
]);

export default router;
