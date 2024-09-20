package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookPage")
public class BookPageController {

    private final BookCommentSqlService bookCommentSqlService;
    private final QuoteService quoteService;

    public BookPageController(BookCommentSqlService bookCommentSqlService, QuoteService quoteService) {
        this.bookCommentSqlService = bookCommentSqlService;
        this.quoteService = quoteService;
    }

    @GetMapping("/switchToComment")
    public ResponseEntity<List<BookCommentDto>> getCommentsByBookId(@RequestParam Long bookId) {
        List<BookCommentDto> comments = bookCommentSqlService.getCommentsByBookId(bookId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/switchToQuote")
    public ResponseEntity<List<QuoteDto>> getQuotesByBookId(@RequestParam Long bookId) {
        List<QuoteDto> quotes = quoteService.getQuotesByBookId(bookId);
        return ResponseEntity.ok(quotes);
    }
}
