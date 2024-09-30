package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByBookId_BookIdOrderByTimestampDesc(Long bookId);

    List<Quote> findByUserId_UserIdOrderByTimestampDesc(Long UserId);

    List<Quote> findBySubCategory_OrderByTimestampDesc(String subCategory);

    List<Quote> findTop6ByOrderByTimestampDesc();

    @Query("SELECT q FROM Quote q WHERE q.subCategory = :subCategory ORDER BY q.timestamp DESC")
    Page<Quote> findBySubCategoryOrderByTimestampDesc(@Param("subCategory") String subCategory, Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.bookId.bookId = :bookId ORDER BY q.timestamp DESC")
    Page<Quote> findByBookIdOrderByTimestampDesc(@Param("bookId") Long bookId, Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.userId.userId = :userId ORDER BY q.timestamp DESC")
    Page<Quote> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);
}
