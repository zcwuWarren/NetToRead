package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
}
