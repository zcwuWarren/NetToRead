package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.CommentDTO;
import com.personal_project.Next_to_read.data.dto.HighlightDTO;
import com.personal_project.Next_to_read.service.BookCommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{category}")
public class BookCommentController {

    private final BookCommentService bookCommentService;

    // Constructor injection
    public BookCommentController(BookCommentService bookCommentService) {
        this.bookCommentService = bookCommentService;
    }

    @PostMapping("/comment")
    public void addOrUpdateComment(@PathVariable String category, @RequestParam String bookId, @RequestBody CommentDTO commentDTO) {
        commentDTO.setBookId(bookId);
        bookCommentService.addOrUpdateComment(commentDTO);
    }

    @PostMapping("/highlight")
    public void addHighlight(@PathVariable String category, @RequestParam String bookId, @RequestBody HighlightDTO highlightDTO) {
        highlightDTO.setBookId(bookId);
        bookCommentService.addHighlight(highlightDTO);
    }
}

