package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.UserBookshelfSql;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookshelfSqlRepository extends JpaRepository<UserBookshelfSql, Long> {

    Optional<UserBookshelfSql> findByUserId_UserIdAndBookId_BookId(Long userId, Long bookId);

    List<UserBookshelfSql> findByUserId_UserIdAndCollectTrueOrderByTimestampCollectDesc(Long userId);

    List<UserBookshelfSql> findByUserId_UserIdAndLikesTrueOrderByTimestampLikeDesc(Long userId);

    List<UserBookshelfSql> findTop4BySubCategoryAndLikesTrueOrderByTimestampLikeDesc(String subCategory);
}

