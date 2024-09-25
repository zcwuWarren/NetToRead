package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.UserBookshelfSql;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookshelfSqlRepository extends JpaRepository<UserBookshelfSql, Long> {

    Optional<UserBookshelfSql> findByUserId_UserIdAndBookId_BookId(Long userId, Long bookId);

    List<UserBookshelfSql> findByUserId_UserIdAndCollectTrueOrderByTimestampCollectDesc(Long userId);

    List<UserBookshelfSql> findByUserId_UserIdAndLikesTrueOrderByTimestampLikeDesc(Long userId);
    // find book by subcategory sort by like timestamp descending
    @Query("SELECT u.bookId.bookId FROM UserBookshelfSql u WHERE u.subCategory = :subCategory ORDER BY u.timestampLike DESC")
    List<Long> findTop6BookIdsBySubCategoryOrderByTimestampLikeDesc(String subCategory);

    @Query("SELECT u.bookId.bookId FROM UserBookshelfSql u WHERE u.subCategory = :subCategory ORDER BY u.timestampCollect DESC")
    List<Long> findTop6BookIdsBySubCategoryOrderByTimestampCollectDesc(String subCategory);

    @Query("SELECT u.bookId.bookId FROM UserBookshelfSql u ORDER BY u.timestampLike DESC")
    List<Long> findTop6BookIdsByOrderByTimestampLikeDesc();

    @Query("SELECT u.bookId.bookId FROM UserBookshelfSql u ORDER BY u.timestampCollect DESC")
    List<Long> findTop6BookIdsOrderByTimestampCollectDesc();
}

