package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.UserBookshelfSql;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBookshelfSqlService {

//    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
//    private final BookCommentSqlRepository bookCommentSqlRepository;
//
//    // constructor injection
//    public UserBookshelfSqlService(UserBookshelfSqlRepository userBookshelfSqlRepository, BookCommentSqlRepository bookCommentSqlRepository) {
//        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
//        this.bookCommentSqlRepository = bookCommentSqlRepository;
//    }
//
//    @Transactional
//    public void addCommentedBook(String userId, String bookId) {
//        BookCommentSql bookCommentSql = bookCommentSqlRepository.findByUserId(userId)
//                .orElse(new UserBookshelfSql());
//        bookCommentSql.setUserId(userId); // 確保 userId 設置正確
//        userBookshelfSql.getCommentedBooks().add(bookId);
//        userBookshelfSqlRepository.save(userBookshelfSql);
//    }
//
//    @Transactional
//    public void addHighlightedBook(String userId, String bookId) {
//        UserBookshelfSql userBookshelfSql = userBookshelfSqlRepository.findByUserId(userId)
//                .orElse(new UserBookshelfSql());
//        userBookshelfSql.setUserId(userId);  // 確保 userId 設置正確
//        userBookshelfSql.getHighlightedBooks().add(bookId);
//        userBookshelfSqlRepository.save(userBookshelfSql);
//    }
}

