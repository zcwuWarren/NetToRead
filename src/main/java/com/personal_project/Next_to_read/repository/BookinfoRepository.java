package com.personal_project.Next_to_read.repository;

import com.personal_project.Next_to_read.model.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookinfoRepository extends JpaRepository<BookInfo, Long> {
}
