package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.TokenDto;
import com.personal_project.Next_to_read.data.form.CommentForm;
import com.personal_project.Next_to_read.data.form.QuoteForm;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookCommentSqlController {

    private final BookCommentSqlService bookCommentSqlService;

    public BookCommentSqlController(BookCommentSqlService bookCommentSqlService) {
        this.bookCommentSqlService = bookCommentSqlService;
    }

    @PostMapping("/addComment")
    public ResponseEntity<?> addOrUpdateComment(@RequestParam Long bookId, @Valid @RequestBody CommentForm commentForm) {

        // get token from localStorage and put into commentForm
        String token = commentForm.getToken();
        boolean success = bookCommentSqlService.addOrUpdateComment(bookId, token, commentForm);

        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "You have already commented on this book"));
        }
        return ResponseEntity.ok(Map.of("message", "Comment Added"));
    }

    @PostMapping("/addQuote")
    public ResponseEntity<?> addQuote(@RequestParam Long bookId, @Valid @RequestBody QuoteForm quoteForm) {

        // get token from localStorage and put into commentForm
        String token = quoteForm.getToken();
        bookCommentSqlService.addQuote(bookId, token, quoteForm);
        return ResponseEntity.ok(Map.of("message", "Quote Added"));
    }
    @PostMapping("/likeBook")
    public ResponseEntity<?> likeBook(@RequestParam Long bookId, @Valid @RequestBody TokenDto tokenDto) {

        // get token from localStorage and put into tokenDto
        String token = tokenDto.getToken();
        boolean success= bookCommentSqlService.likeBook(bookId, token);

        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Cancel like"));
        }
        return ResponseEntity.ok(Map.of("message", "Liked!"));
    }

    @PostMapping("/collectBook")
    public ResponseEntity<?> collectBook(@RequestParam Long bookId, @Valid @RequestBody TokenDto tokenDto) {

        // get token from localStorage and put into tokenDto
        String token = tokenDto.getToken();
        boolean success= bookCommentSqlService.collectBook(bookId, token);

        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Cancel collect"));
        }
        return ResponseEntity.ok(Map.of("message", "Collected!"));
    }
}
