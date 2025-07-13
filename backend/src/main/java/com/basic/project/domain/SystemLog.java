package com.basic.project.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {
    private Long id;
    private String level;
    private String username;
    private String action;
    private String message;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private String details;
}