package com.personal_project.Next_to_read.controller;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import com.personal_project.Next_to_read.service.BookCommentSqlService;
import com.personal_project.Next_to_read.service.BookPageService;
import com.personal_project.Next_to_read.service.QuoteService;
import com.personal_project.Next_to_read.service.UserBookshelfSqlService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookPage")
public class BookPageController {

    private final BookCommentSqlService bookCommentSqlService;
    private final QuoteService quoteService;
    private final BookInfoRepository bookInfoRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final UserBookshelfSqlService userBookshelfSqlService;
    private final BookPageService bookPageService;

    public BookPageController(BookCommentSqlService bookCommentSqlService, QuoteService quoteService, BookInfoRepository bookInfoRepository, UserBookshelfSqlRepository userBookshelfSqlRepository, UserBookshelfSqlService userBookshelfSqlService, BookPageService bookPageService) {
        this.bookCommentSqlService = bookCommentSqlService;
        this.quoteService = quoteService;
        this.bookInfoRepository = bookInfoRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.userBookshelfSqlService = userBookshelfSqlService;
        this.bookPageService = bookPageService;
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

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = bookPageService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("{mainCategory}/{subCategory}/latest-likes-by-category")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooksByCategory(@PathVariable String subCategory) {
        List<BookInfoDto> latestLikedBooksByCategory = bookPageService.getTop6BooksByLikesByCategory(subCategory);
        return ResponseEntity.ok(latestLikedBooksByCategory);
    }

    @GetMapping("/{mainCategory}/{subCategory}/latest-comments")
    public ResponseEntity<List<BookCommentDto>> getLatestComments(
            @PathVariable String subCategory) {
        List<BookCommentDto> latestComments = bookPageService.getLatestCommentsBySubCategory(subCategory);
        return ResponseEntity.ok(latestComments);
    }

    @GetMapping("/latest-likes")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooks() {
        List<BookInfoDto> latestLikedBooks = bookPageService.getTop6BooksByLikes();
        return ResponseEntity.ok(latestLikedBooks);
    }

    @GetMapping("/getBookInfo")
    public ResponseEntity<BookInfoDto> getBookInfoById(@RequestParam Long bookId) {
        BookInfoDto bookInfoDto = bookPageService.getBookInfoById(bookId);
        return ResponseEntity.ok(bookInfoDto);
    }

    @GetMapping("/{mainCategory}/{subCategory}")
    public String showCategoryPage(@PathVariable String mainCategory, @PathVariable String subCategory, Model model) {
        // send mainCategory, subCategory to category model
        model.addAttribute("mainCategory", mainCategory);
        model.addAttribute("subCategory", subCategory);
        return "category";
    }
}
