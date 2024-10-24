package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.UserBookshelfSql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookshelfSqlRepository extends JpaRepository<UserBookshelfSql, Long> {

    Optional<UserBookshelfSql> findByUserId_UserIdAndBookId_BookId(Long userId, Long bookId);

    @Query(value = "SELECT ubs.book_id " +
            "FROM user_bookshelf ubs " +
            "WHERE ubs.likes = true " +
            "GROUP BY ubs.book_id " +
            "ORDER BY MAX(ubs.timestamp_like) DESC",
            countQuery = "SELECT COUNT(DISTINCT ubs.book_id) FROM user_bookshelf ubs WHERE ubs.likes = true",
            nativeQuery = true)
    Page<Long> findBookIdsByOrderByTimestampLikeDesc(Pageable pageable);

    @Query(value = "SELECT ubs.book_id " +
            "FROM user_bookshelf ubs " +
            "WHERE ubs.collect = true " +
            "GROUP BY ubs.book_id " +
            "ORDER BY MAX(ubs.timestamp_collect) DESC",
            countQuery = "SELECT COUNT(DISTINCT ubs.book_id) FROM user_bookshelf ubs WHERE ubs.collect = true",
            nativeQuery = true)
    Page<Long> findBookIdsByOrderByTimestampCollectDesc(Pageable pageable);

    @Query(value = "SELECT ubs.book_id " +
            "FROM user_bookshelf ubs " +
            "JOIN book_info bi ON ubs.book_id = bi.book_id " +
            "WHERE ubs.likes = true " +
            "AND bi.sub_category = :subCategory " +
            "GROUP BY ubs.book_id " +
            "ORDER BY MAX(ubs.timestamp_like) DESC",
            countQuery = "SELECT COUNT(DISTINCT ubs.book_id) " +
                    "FROM user_bookshelf ubs " +
                    "JOIN book_info bi ON ubs.book_id = bi.book_id " +
                    "WHERE ubs.likes = true " +
                    "AND bi.sub_category = :subCategory",
            nativeQuery = true)
    Page<Long> findBookIdsBySubCategoryOrderByTimestampLikeDesc(@Param("subCategory") String subCategory, Pageable pageable
    );

    @Query(value = "SELECT ubs.book_id " +
            "FROM user_bookshelf ubs " +
            "JOIN book_info bi ON ubs.book_id = bi.book_id " +
            "WHERE ubs.collect = true " +
            "AND bi.sub_category = :subCategory " +
            "GROUP BY ubs.book_id " +
            "ORDER BY MAX(ubs.timestamp_collect) DESC",
            countQuery = "SELECT COUNT(DISTINCT ubs.book_id) " +
                    "FROM user_bookshelf ubs " +
                    "JOIN book_info bi ON ubs.book_id = bi.book_id " +
                    "WHERE ubs.collect = true " +
                    "AND bi.sub_category = :subCategory",
            nativeQuery = true)
    Page<Long> findBookIdsBySubCategoryOrderByTimestampCollectDesc(@Param("subCategory") String subCategory, Pageable pageable
    );

    @Query(value = "SELECT ubs FROM UserBookshelfSql ubs " +
            "JOIN FETCH ubs.bookId " +
            "WHERE ubs.userId.userId = :userId " +
            "AND ubs.likes = true " +
            "ORDER BY ubs.timestampLike DESC")
    Page<UserBookshelfSql> findLikedBooksByUserId(@Param("userId") Long userId, Pageable pageable
    );

    @Query(value = "SELECT ubs FROM UserBookshelfSql ubs " +
            "JOIN FETCH ubs.bookId " +
            "WHERE ubs.userId.userId = :userId " +
            "AND ubs.collect = true " +
            "ORDER BY ubs.timestampCollect DESC")
    Page<UserBookshelfSql> findCollectedBooksByUserId(@Param("userId") Long userId, Pageable pageable
    );

    @Query("SELECT ubs.likes FROM UserBookshelfSql ubs " +
            "WHERE ubs.userId.userId = :userId AND ubs.bookId.bookId = :bookId")
    Boolean findLikeStatusByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT ubs.collect FROM UserBookshelfSql ubs " +
            "WHERE ubs.userId.userId = :userId AND ubs.bookId.bookId = :bookId")
    Boolean findCollectStatusByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT ubs FROM UserBookshelfSql ubs " +
            "WHERE ubs.likes = true " +
            "ORDER BY ubs.timestampLike DESC")
    Page<UserBookshelfSql> findByLikesTrueOrderByTimestampLikeDesc(Pageable pageable);

    @Query("SELECT ubs FROM UserBookshelfSql ubs " +
            "WHERE ubs.collect = true " +
            "ORDER BY ubs.timestampCollect DESC")
    Page<UserBookshelfSql> findByCollectsTrueOrderByTimestampCollectDesc(Pageable pageable);
}
