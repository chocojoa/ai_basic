package com.basic.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForceChangePasswordRequest {
    
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String newPassword;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String confirmPassword;
}