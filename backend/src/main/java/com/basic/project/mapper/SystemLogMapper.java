package com.basic.project.mapper;

import com.basic.project.domain.SystemLog;
import com.basic.project.dto.LogSearchRequest;
import com.basic.project.dto.LogStatsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SystemLogMapper {
    
    void insert(SystemLog systemLog);
    
    List<SystemLog> findAll();
    
    List<SystemLog> findWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    List<SystemLog> search(LogSearchRequest request);
    
    int count();
    
    int countBySearch(LogSearchRequest request);
    
    long countByLevel(@Param("level") String level);
    
    LogStatsResponse getStats();
    
    int getTodayLogsCount();
    
    int countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    void deleteOldLogs(@Param("days") int days);
}