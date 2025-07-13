package com.basic.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "사용자명을 입력해주세요")
    @Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^\\w+$", message = "사용자명은 영문, 숫자, 밑줄(_)만 사용할 수 있습니다")
    private String username;
    
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 6, max = 100, message = "비밀번호는 6자 이상 100자 이하여야 합니다")
    private String password;
    
    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String confirmPassword;
    
    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다")
    private String fullName;
    
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "전화번호는 숫자, 하이픈(-), 공백만 사용할 수 있습니다")
    private String phone;
    
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}