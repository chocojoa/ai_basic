package com.basic.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogSearchRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String level;
    private String username;
    private String action;
    private String search;
    private int page = 0;
    private int size = 20;
}