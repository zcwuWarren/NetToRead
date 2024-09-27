package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.*;
import com.personal_project.Next_to_read.data.form.CommentForm;
import com.personal_project.Next_to_read.data.form.QuoteForm;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookCommentSqlController {

    private final BookCommentSqlService bookCommentSqlService;
    private final QuoteService quoteService;

    public BookCommentSqlController(BookCommentSqlService bookCommentSqlService, QuoteService quoteService) {
        this.bookCommentSqlService = bookCommentSqlService;
        this.quoteService = quoteService;
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

    @PostMapping("/deleteComment")
    public ResponseEntity<?> deleteComment(@RequestBody DeleteCommentDto request) {
        boolean isDeleted = bookCommentSqlService.deleteComment(request.getId(), request.getToken());

        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized to delete this comment"));
        }
    }

    @PostMapping("/deleteQuote")
    public ResponseEntity<?> deleteQuote(@RequestBody DeleteQuoteDto request) {
        boolean isDeleted = quoteService.deleteQuote(request.getId(), request.getToken());

        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "Quote deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized to delete this quote"));
        }
    }

    @PostMapping("/editComment")
    public ResponseEntity<?> editComment(@RequestBody EditCommentDto request) {
        boolean isEdited = bookCommentSqlService.editComment(request.getId(), request.getToken(), request.getUpdatedComment());

        if (isEdited) {
            return ResponseEntity.ok(Map.of("message", "Comment updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized to edit this comment"));
        }
    }

    @PostMapping("/editQuote")
    public ResponseEntity<?> editQuote(@RequestBody EditQuoteDto request) {
        boolean isEdited = quoteService.editQuote(request.getId(), request.getToken(), request.getUpdatedQuote());

        if (isEdited) {
            return ResponseEntity.ok(Map.of("message", "Quote updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized to edit this comment"));
        }
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
