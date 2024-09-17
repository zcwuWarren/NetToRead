package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookCommentSql;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCommentSqlRepository extends JpaRepository<BookCommentSql, Long> {
    // 你可以根據需要添加自定義查詢方法
}