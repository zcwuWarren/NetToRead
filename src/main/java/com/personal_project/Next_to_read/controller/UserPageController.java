package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.data.dto.TokenDto;
import com.personal_project.Next_to_read.data.dto.UserBookshelfDto;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.BookPageService;
import com.personal_project.Next_to_read.service.QuoteService;
import com.personal_project.Next_to_read.service.UserBookshelfSqlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/userPage")
public class UserPageController {

    private final QuoteService quoteService;
    private final BookCommentSqlService bookCommentSqlService;
    private final UserBookshelfSqlService userBookshelfSqlService;
    private final BookPageService bookPageService;

    public UserPageController(QuoteService quoteService, BookCommentSqlService bookCommentSqlService, UserBookshelfSqlService userBookshelfSqlService, BookPageService bookPageService) {
        this.quoteService = quoteService;
        this.bookCommentSqlService = bookCommentSqlService;
        this.userBookshelfSqlService = userBookshelfSqlService;
        this.bookPageService = bookPageService;
    }

    @PostMapping("/myQuote")
    public ResponseEntity<List<QuoteDto>> getQuotesByUserId(@Valid @RequestBody TokenDto tokenDto,
                                                            @RequestParam(defaultValue = "0") int offset,
                                                            @RequestParam(defaultValue = "50") int limit) {

        String token = tokenDto.getToken();
        List<QuoteDto> quotes = quoteService.getQuotesByUserId(token, offset, limit);
        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/myComment")
    public ResponseEntity<List<BookCommentDto>> getCommentsByUserId(@Valid @RequestBody TokenDto tokenDto,
                                                                    @RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "50") int limit) {

        String token = tokenDto.getToken();
        List<BookCommentDto> comments = bookPageService.getCommentsByUserId(token, offset, limit);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/myCollect")
    public ResponseEntity<List<UserBookshelfDto>> getCollectByUserId(@Valid @RequestBody TokenDto tokenDto,
                                                                     @RequestParam(defaultValue = "0") int offset,
                                                                     @RequestParam(defaultValue = "50") int limit) {

        String token = tokenDto.getToken();
        List<UserBookshelfDto> collects = userBookshelfSqlService.getCollectByUserId(token, offset, limit);
        return ResponseEntity.ok(collects);
    }

    @PostMapping("/myLike")
    public ResponseEntity<List<UserBookshelfDto>> getLikeByUserId(@Valid @RequestBody TokenDto tokenDto,
                                                                  @RequestParam(defaultValue = "0") int offset,
                                                                  @RequestParam(defaultValue = "50") int limit) {

        String token = tokenDto.getToken();
        List<UserBookshelfDto> likes = userBookshelfSqlService.getLikeByUserId(token, offset, limit);
        return ResponseEntity.ok(likes);
    }

    @PostMapping("/userInteraction")
    public ResponseEntity<?> getUserBookInteraction(@RequestParam Long bookId, @RequestBody TokenDto tokenDto) {
        try {
            String token = tokenDto.getToken();
            boolean isLiked = userBookshelfSqlService.isBookLikedByUser(bookId, token);
            boolean isCollected = userBookshelfSqlService.isBookCollectedByUser(bookId, token);

            Map<String, Boolean> result = new HashMap<>();
            result.put("liked", isLiked);
            result.put("collected", isCollected);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking user interaction");
        }
    }
}
