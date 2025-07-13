package com.basic.project.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Menu {
    private Long id;
    private String menuName;
    private Long parentId;
    private String url;
    private String icon;
    private Integer orderNum;
    private Boolean isVisible;
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<Menu> children;
    private RoleMenu permission;
}