package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookCommentSql;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCommentSqlRepository extends JpaRepository<BookCommentSql, Long> {

    Optional<BookCommentSql> findByUserId_UserIdAndBookId_BookId(Long userId, Long bookId);

    List<BookCommentSql> findByBookId_BookIdOrderByTimestampDesc(Long bookId);

    List<BookCommentSql> findByUserId_UserIdOrderByTimestampDesc(Long userId);
}