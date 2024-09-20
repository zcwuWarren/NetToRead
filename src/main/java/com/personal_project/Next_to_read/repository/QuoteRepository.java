package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByBook_BookId(Long bookId);
}
