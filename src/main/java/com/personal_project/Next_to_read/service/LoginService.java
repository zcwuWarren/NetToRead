package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.LoginDto;

public interface LoginService {
    LoginDto userLoginNative(String email, String password);
}
