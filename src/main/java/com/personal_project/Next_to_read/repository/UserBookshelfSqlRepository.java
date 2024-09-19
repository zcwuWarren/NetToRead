package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.UserBookshelfSql;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBookshelfSqlRepository extends JpaRepository<UserBookshelfSql, Long> {
//    Optional<UserBookshelfSql> findByUserId(String userId);
    // 你可以根據需要添加自定義查詢方法
}

