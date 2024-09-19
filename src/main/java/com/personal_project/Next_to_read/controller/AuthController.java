package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.LoginDto;
import com.personal_project.Next_to_read.data.form.LoginForm;
import com.personal_project.Next_to_read.data.form.RegistrationForm;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.service.LoginService;
import com.personal_project.Next_to_read.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;
    private final UserService userService;

    public AuthController(UserService userService, LoginService loginService) {
        this.userService = userService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationForm registerForm) {
        @SuppressWarnings("unused")
        User registeredUser = userService.registerUser(registerForm);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginForm loginForm) {
        LoginDto logindto = loginService.userLoginNative(loginForm.getEmail(), loginForm.getPassword());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("data", logindto));
    }

}
