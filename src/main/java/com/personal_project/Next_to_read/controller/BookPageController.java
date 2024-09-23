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



    // categories
    @GetMapping("/{mainCategory}/{subCategory}")
    public String showCategoryPage(@PathVariable String mainCategory, @PathVariable String subCategory, Model model) {
        // send mainCategory, subCategory to category model
        model.addAttribute("mainCategory", mainCategory);
        model.addAttribute("subCategory", subCategory);
        return "category";
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = bookPageService.getCategories();
        return ResponseEntity.ok(categories);
    }

    // likes
    // likes by subCategory
    @GetMapping("/{mainCategory}/{subCategory}/latest-likes-by-category")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooksByCategory(@PathVariable String subCategory) {
        List<BookInfoDto> latestLikedBooks = bookPageService.getLatestLikedBooksByCategory(subCategory);
        return ResponseEntity.ok(latestLikedBooks);
    }

    // likes without condition
    @GetMapping("/latest-likes")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooks() {
        List<BookInfoDto> latestLikedBooks = bookPageService.getLatestLikedBooks();
        return ResponseEntity.ok(latestLikedBooks);
    }


    // collects
    // collect by subCategory
    @GetMapping("/{mainCategory}/{subCategory}/latest-collect-by-category")
    public ResponseEntity<List<BookInfoDto>> getLatestCollectBooksByCategory(@PathVariable String subCategory) {
        List<BookInfoDto> latestCollectBooksByCategory = bookPageService.getTop6BooksByCategory(subCategory);
        return ResponseEntity.ok(latestCollectBooksByCategory);
    }

    // collect without condition








    // comments
    // comment by bookId
    @GetMapping("/switchToComment")
    public ResponseEntity<List<BookCommentDto>> getCommentsByBookId(@RequestParam Long bookId) {
        List<BookCommentDto> comments = bookCommentSqlService.getCommentsByBookId(bookId);
        return ResponseEntity.ok(comments);
    }

    // comment by subCategory
    @GetMapping("/{mainCategory}/{subCategory}/latest-comments")
    public ResponseEntity<List<BookCommentDto>> getLatestCommentsBySubCategory(@PathVariable String subCategory) {
        List<BookCommentDto> comments = bookPageService.getLatestCommentsBySubCategory(subCategory);
        return ResponseEntity.ok(comments);
    }

    // comment without condition
    @GetMapping("/latest-comments")
    public ResponseEntity<List<BookCommentDto>> getLatestComments() {
        List<BookCommentDto> comments = bookPageService.getLatestComments();
        return ResponseEntity.ok(comments);
    }



    // quotes
    // quotes by bookId
    @GetMapping("/switchToQuote")
    public ResponseEntity<List<QuoteDto>> getQuotesByBookId(@RequestParam Long bookId) {
        List<QuoteDto> quotes = quoteService.getQuotesByBookId(bookId);
        return ResponseEntity.ok(quotes);
    }

    // quotes by sub-category
    @GetMapping("{mainCategory}/{subCategory}/latest-quotes")
    public ResponseEntity<List<QuoteDto>> getLatestQuotesBySubCategory(@PathVariable String subCategory) {
        List<QuoteDto> quotes = quoteService.getQuotesBySubCategory(subCategory);
        return ResponseEntity.ok(quotes);
    }

    // quotes without condition
    @GetMapping("/latest-quotes")
    public ResponseEntity<List<QuoteDto>> getLatestQuotes() {
        List<QuoteDto> quotes = quoteService.getQuotesWithoutCondition();
        return ResponseEntity.ok(quotes);
    }

    // detail of books by bookId
    @GetMapping("/getBookInfo")
    public ResponseEntity<BookInfoDto> getBookInfoById(@RequestParam Long bookId) {
        BookInfoDto bookInfoDto = bookPageService.getBookInfoById(bookId);
        return ResponseEntity.ok(bookInfoDto);
    }
}
