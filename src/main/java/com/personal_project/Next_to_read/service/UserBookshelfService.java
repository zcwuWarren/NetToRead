package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.CommentDTO;
import com.personal_project.Next_to_read.data.dto.HighlightDTO;
import com.personal_project.Next_to_read.model.BookComment;
import com.personal_project.Next_to_read.repository.BookCommentRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBookshelfService {

    private final UserBookshelfRepository userBookshelfRepository;

    public UserBookshelfService(UserBookshelfRepository userBookshelfRepository) {
        this.userBookshelfRepository = userBookshelfRepository;
    }

    @Transactional
    public void addCommentedBook(String userId, String bookId) {
        userBookshelfRepository.addCommentedBook(userId, bookId);
    }

    @Transactional
    public void addHighlightedBook(String userId, String bookId) {
        userBookshelfRepository.addHighlightedBook(userId, bookId);
    }
}
