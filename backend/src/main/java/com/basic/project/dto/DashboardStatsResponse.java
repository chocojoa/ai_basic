package com.basic.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private int totalUsers;
    private int activeUsers;
    private int inactiveUsers;
    private int totalRoles;
    private int activeRoles;
    private int totalMenus;
    private int visibleMenus;
    private int totalPermissions;
    private int totalLogs;
    private int todayLogs;
}