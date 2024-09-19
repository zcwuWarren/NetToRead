package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_project.Next_to_read.data.dto.LoginDto;
import com.personal_project.Next_to_read.exception.auth.UserNotExistException;
import com.personal_project.Next_to_read.exception.auth.UserPasswordMismatchException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    private static final long SESSION_TIMEOUT_MINUTES = 30;

    @Autowired
    public LoginServiceImpl(UserRepository userRepository,
                            ObjectMapper objectMapper,
                            PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public LoginDto userLoginNative(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Check if user exists
        User user = optionalUser.orElseThrow(() ->
                new UserNotExistException("帳號或密碼錯誤")
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserPasswordMismatchException("帳號或密碼錯誤");
        }
        String jwtToken = jwtTokenUtil.generateToken(user);
        Long accessExpired = jwtTokenUtil.getExpirationDateFromToken(jwtToken).getTime();

        LoginDto loginDto = new LoginDto(jwtToken, accessExpired, user);
        log.info("logged in:{}", loginDto);
        return loginDto;
    }
}
