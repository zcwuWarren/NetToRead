package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.data.dto.TokenDto;
import com.personal_project.Next_to_read.data.dto.UserBookshelfDto;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.QuoteService;
import com.personal_project.Next_to_read.service.UserBookshelfSqlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userPage")
public class UserPageController {

    private final QuoteService quoteService;
    private final BookCommentSqlService bookCommentSqlService;
    private final UserBookshelfSqlService userBookshelfSqlService;

    public UserPageController(QuoteService quoteService, BookCommentSqlService bookCommentSqlService, UserBookshelfSqlService userBookshelfSqlService) {
        this.quoteService = quoteService;
        this.bookCommentSqlService = bookCommentSqlService;
        this.userBookshelfSqlService = userBookshelfSqlService;
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

    @PostMapping("/myCollect")
    public ResponseEntity<List<UserBookshelfDto>> getCollectByUserId(@Valid @RequestBody TokenDto tokenDto) {

        String token = tokenDto.getToken();
        List<UserBookshelfDto> collects = userBookshelfSqlService.getCollectByUserId(token);
        return ResponseEntity.ok(collects);
    }

    @PostMapping("/myLike")
    public ResponseEntity<List<UserBookshelfDto>> getLikeByUserId(@Valid @RequestBody TokenDto tokenDto) {

        String token = tokenDto.getToken();
        List<UserBookshelfDto> likes = userBookshelfSqlService.getLikeByUserId(token);
        return ResponseEntity.ok(likes);
    }

}
