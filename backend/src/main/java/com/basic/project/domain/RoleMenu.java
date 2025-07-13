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
public class RoleMenu {
    private Long id;
    private Long roleId;
    private Long menuId;
    private Boolean canRead;
    private Boolean canWrite;
    private Boolean canDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}