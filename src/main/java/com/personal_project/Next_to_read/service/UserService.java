package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.form.RegisterForm;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterForm form) {

        if (userRepository.existsByEmail(form.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(form.getUserName());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        return userRepository.save(user);
    }
}
