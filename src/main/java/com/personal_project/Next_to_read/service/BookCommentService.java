package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.CommentDTO;
import com.personal_project.Next_to_read.data.dto.HighlightDTO;
import com.personal_project.Next_to_read.model.BookComment;
import com.personal_project.Next_to_read.repository.BookCommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookCommentService {

    private final BookCommentRepository bookCommentRepository;
    private final UserBookshelfService userBookshelfService;

    // Constructor injection
    public BookCommentService(BookCommentRepository bookCommentRepository, UserBookshelfService userBookshelfService) {
        this.bookCommentRepository = bookCommentRepository;
        this.userBookshelfService = userBookshelfService;
    }

    @Transactional
    public void addOrUpdateComment(CommentDTO commentDTO) {
        String bookId = commentDTO.getBookId();
        String userId = commentDTO.getUserId();
        String comment = commentDTO.getComment();

        BookComment bookComment = bookCommentRepository.findById(bookId).orElse(null);
        if (bookComment != null) {
            bookCommentRepository.addOrUpdateComment(bookId, userId, comment);
        } else {
            BookComment newComment = new BookComment();
            newComment.setBookId(bookId);
            newComment.setUserComments(List.of(new BookComment.UserComment(userId, comment, null)));
            bookCommentRepository.save(newComment);
        }

        // 同時更新 UserBookshelf
        userBookshelfService.addCommentedBook(userId, bookId);
    }

    @Transactional
    public void addHighlight(HighlightDTO highlightDTO) {
        String bookId = highlightDTO.getBookId();
        String userId = highlightDTO.getUserId();
        List<String> highlights = highlightDTO.getHighlights();

        for (String highlight : highlights) {
            bookCommentRepository.addHighlight(bookId, userId, highlight);
        }

        // 同時更新 UserBookshelf
        userBookshelfService.addHighlightedBook(userId, bookId);
    }
}