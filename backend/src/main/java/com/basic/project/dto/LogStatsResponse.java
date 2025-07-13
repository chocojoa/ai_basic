package com.basic.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogStatsResponse {
    private long total;
    private long info;
    private long warning;
    private long error;
}