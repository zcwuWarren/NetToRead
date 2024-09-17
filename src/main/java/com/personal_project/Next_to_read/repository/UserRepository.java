package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
