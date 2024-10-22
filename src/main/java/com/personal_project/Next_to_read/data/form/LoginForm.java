package com.personal_project.Next_to_read.data.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginForm {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
