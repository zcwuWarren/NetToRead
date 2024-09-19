package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {

    Optional<BookInfo> findByBookId(long bookId);
}