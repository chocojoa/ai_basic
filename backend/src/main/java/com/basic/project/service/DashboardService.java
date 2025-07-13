package com.basic.project.service;

import com.basic.project.dto.DashboardStatsResponse;
import com.basic.project.mapper.UserMapper;
import com.basic.project.mapper.RoleMapper;
import com.basic.project.mapper.MenuMapper;
import com.basic.project.mapper.RoleMenuMapper;
import com.basic.project.mapper.SystemLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final SystemLogMapper systemLogMapper;

    public DashboardStatsResponse getDashboardStats() {
        log.info("대시보드 통계 데이터 조회 시작");

        try {
            var userStats = getUserStatistics();
            var roleStats = getRoleStatistics();
            var menuStats = getMenuStatistics();
            var permissionStats = getPermissionStatistics();
            var logStats = getLogStatistics();

            DashboardStatsResponse stats = buildStatsResponse(userStats, roleStats, menuStats, permissionStats, logStats);

            log.info("대시보드 통계 데이터 조회 완료: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("대시보드 통계 조회 중 오류 발생", e);
            throw new IllegalStateException("대시보드 통계 조회에 실패했습니다", e);
        }
    }

    private UserStatistics getUserStatistics() {
        int totalUsers = userMapper.count();
        int activeUsers = userMapper.findByActive(true).size();
        int inactiveUsers = totalUsers - activeUsers;
        return new UserStatistics(totalUsers, activeUsers, inactiveUsers);
    }

    private RoleStatistics getRoleStatistics() {
        int totalRoles = roleMapper.count();
        int activeRoles = roleMapper.findByActive(true).size();
        return new RoleStatistics(totalRoles, activeRoles);
    }

    private MenuStatistics getMenuStatistics() {
        int totalMenus = menuMapper.count();
        int visibleMenus = menuMapper.findVisibleMenus().size();
        return new MenuStatistics(totalMenus, visibleMenus);
    }

    private int getPermissionStatistics() {
        return roleMenuMapper.count();
    }

    private LogStatistics getLogStatistics() {
        int totalLogs = systemLogMapper.count();
        int todayLogs = systemLogMapper.getTodayLogsCount();
        return new LogStatistics(totalLogs, todayLogs);
    }

    private DashboardStatsResponse buildStatsResponse(UserStatistics userStats, RoleStatistics roleStats, 
                                                     MenuStatistics menuStats, int totalPermissions, LogStatistics logStats) {
        return DashboardStatsResponse.builder()
            .totalUsers(userStats.total())
            .activeUsers(userStats.active())
            .inactiveUsers(userStats.inactive())
            .totalRoles(roleStats.total())
            .activeRoles(roleStats.active())
            .totalMenus(menuStats.total())
            .visibleMenus(menuStats.visible())
            .totalPermissions(totalPermissions)
            .totalLogs(logStats.total())
            .todayLogs(logStats.today())
            .build();
    }

    private record UserStatistics(int total, int active, int inactive) {}
    private record RoleStatistics(int total, int active) {}
    private record MenuStatistics(int total, int visible) {}
    private record LogStatistics(int total, int today) {}
}