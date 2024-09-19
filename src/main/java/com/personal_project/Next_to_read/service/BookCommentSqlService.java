package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.CommentDto;
import com.personal_project.Next_to_read.data.dto.HighlightDto;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookCommentSqlService {

    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlService userBookshelfSqlService;

    public BookCommentSqlService(BookCommentSqlRepository bookCommentSqlRepository, UserBookshelfSqlService userBookshelfSqlService) {
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlService = userBookshelfSqlService;
    }

    @Transactional
    public void addOrUpdateComment(CommentDto commentDTO) {
        // 查找或新建 BookCommentSql
        BookCommentSql bookCommentSql = bookCommentSqlRepository.findById()
                .orElse(new BookCommentSql());
        bookCommentSql.setBookId(commentDTO.getBookId());
        bookCommentSql.setUserId(commentDTO.getUserId());
        bookCommentSql.setComment(commentDTO.getComment());
        bookCommentSqlRepository.save(bookCommentSql);

        // 同步更新 UserBookshelf
        userBookshelfSqlService.addCommentedBook(commentDTO.getUserId(), commentDTO.getBookId());
    }

    @Transactional
    public void addHighlight(HighlightDto highlightDTO) {
        BookCommentSql bookCommentSql = bookCommentSqlRepository.findById(Long.parseLong(highlightDTO.getBookId()))
                .orElse(new BookCommentSql());
        bookCommentSql.setBookId(highlightDTO.getBookId());
        bookCommentSql.setUserId(highlightDTO.getUserId());
        bookCommentSql.getHighlights().addAll(highlightDTO.getHighlights());
        bookCommentSqlRepository.save(bookCommentSql);

        // 同步更新 UserBookshelf
        userBookshelfSqlService.addHighlightedBook(highlightDTO.getUserId(), highlightDTO.getBookId());
    }
}

