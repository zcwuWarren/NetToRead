package com.personal_project.Next_to_read.data.form;

import com.personal_project.Next_to_read.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterForm {

    @NotBlank
    private String userName;

    @Email(message = "請輸入有效的電子郵件地址") // @Email for validating email format
    @NotBlank(message = "電子郵件地址不能為空") // @NotBlank for validating no blank
    private String email;

    @NotBlank(message = "密碼不能為空")
    @ValidPassword // for custom password rule
    private String password;
}
