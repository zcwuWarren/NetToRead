package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.CommentDTO;
import com.personal_project.Next_to_read.data.dto.HighlightDTO;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{category}")
public class BookCommentSqlController {

    private final BookCommentSqlService bookCommentSqlService;

    public BookCommentSqlController(BookCommentSqlService bookCommentSqlService) {
        this.bookCommentSqlService = bookCommentSqlService;
    }

    @PostMapping("/comment")
    public void addOrUpdateComment(@PathVariable String category, @RequestParam String bookId, @RequestBody CommentDTO commentDTO) {
        commentDTO.setBookId(bookId);
        bookCommentSqlService.addOrUpdateComment(commentDTO);
    }

    @PostMapping("/highlight")
    public void addHighlight(@PathVariable String category, @RequestParam String bookId, @RequestBody HighlightDTO highlightDTO) {
        highlightDTO.setBookId(bookId);
        bookCommentSqlService.addHighlight(highlightDTO);
    }
}
