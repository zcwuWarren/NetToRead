package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookCommentSqlRepository extends JpaRepository<BookCommentSql, Long> {

    Optional<BookCommentSql> findByUserId_UserIdAndBookId_BookId(Long userId, Long bookId);

    @Query("SELECT bcs FROM BookCommentSql bcs WHERE bcs.subCategory = :subCategory ORDER BY bcs.timestamp DESC")
    Page<BookCommentSql> findBySubCategoryOrderByTimestampDesc(@Param("subCategory") String subCategory, Pageable pageable);

    @Query("SELECT bcs FROM BookCommentSql bcs WHERE bcs.bookId.bookId = :bookId ORDER BY bcs.timestamp DESC")
    Page<BookCommentSql> findByBookIdOrderByTimestampDesc(@Param("bookId") Long bookId, Pageable pageable);

    @Query("SELECT bcs FROM BookCommentSql bcs WHERE bcs.userId.userId = :userId ORDER BY bcs.timestamp DESC")
    Page<BookCommentSql> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);

    List<BookCommentSql> findFirstByTimestampLessThanOrderByTimestampDesc(Timestamp timestamp, Pageable pageable);
}