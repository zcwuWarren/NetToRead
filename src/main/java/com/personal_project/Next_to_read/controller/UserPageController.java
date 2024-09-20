package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.data.dto.TokenDto;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userPage")
public class UserPageController {

    private final QuoteService quoteService;
    private final BookCommentSqlService bookCommentSqlService;

    public UserPageController(QuoteService quoteService, BookCommentSqlService bookCommentSqlService) {
        this.quoteService = quoteService;
        this.bookCommentSqlService = bookCommentSqlService;
    }

    @PostMapping("/myQuote")
    public ResponseEntity<List<QuoteDto>> getQuotesByUserId(@Valid @RequestBody TokenDto tokenDto) {

        String token = tokenDto.getToken();
        List<QuoteDto> quotes = quoteService.getQuotesByUserId(token);
        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/myComment")
    public ResponseEntity<List<BookCommentDto>> getCommentsByUserId(@Valid @RequestBody TokenDto tokenDto) {

        String token = tokenDto.getToken();
        List<BookCommentDto> comments = bookCommentSqlService.getCommentsByUserId(token);
        return ResponseEntity.ok(comments);
    }
}
