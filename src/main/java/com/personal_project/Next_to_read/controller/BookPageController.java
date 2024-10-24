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
import org.springframework.data.domain.Page;
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
    @GetMapping("/latest-likes-by-subCategory")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooksByCategory(@RequestParam String subCategory,
                                                                           @RequestParam(defaultValue = "0") int offset,
                                                                           @RequestParam(defaultValue = "50") int limit) {
        List<BookInfoDto> latestLikedBooks = bookPageService.getLatestLikedBooksByCategory(subCategory, offset, limit);
        return ResponseEntity.ok(latestLikedBooks);
    }

    // likes without condition
    @GetMapping("/latest-likes")
    public ResponseEntity<List<BookInfoDto>> getLatestLikedBooks(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<BookInfoDto> latestLikedBooks = bookPageService.getLatestLikedBooks(offset, limit);
        return ResponseEntity.ok(latestLikedBooks);
    }

    // collects
    // collect by subCategory
    @GetMapping("/latest-collect-by-subCategory")
    public ResponseEntity<List<BookInfoDto>> getLatestCollectBooksByCategory(@RequestParam String subCategory,
                                                                             @RequestParam(defaultValue = "0") int offset,
                                                                             @RequestParam(defaultValue = "50") int limit) {
        List<BookInfoDto> latestCollectBooksByCategory = bookPageService.getLatestCollectBooksByCategory(subCategory, offset, limit);
        return ResponseEntity.ok(latestCollectBooksByCategory);
    }

    // collect without condition
    @GetMapping("/latest-collect")
    public ResponseEntity<List<BookInfoDto>> getLatestCollectBooks(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<BookInfoDto> latestCollectedBooks = bookPageService.getLatestCollectBooks(offset, limit);
        return ResponseEntity.ok(latestCollectedBooks);
    }

    // comments
    // comment by bookId
    @GetMapping("/switchToComment")
    public ResponseEntity<List<BookCommentDto>> getCommentsByBookId(@RequestParam Long bookId,
                                                                    @RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "50") int limit) {
        List<BookCommentDto> comments = bookPageService.getCommentsByBookId(bookId, offset, limit);
        return ResponseEntity.ok(comments);
    }

    // comment by subCategory
    @GetMapping("/latest-comments-by-subCategory")
    public ResponseEntity<List<BookCommentDto>> getCommentsBySubCategory(@RequestParam String subCategory,
                                                                       @RequestParam(defaultValue = "0") int offset,
                                                                       @RequestParam(defaultValue = "50") int limit) {
        List<BookCommentDto> comments = bookPageService.getCommentsBySubCategory(subCategory, offset, limit);
        return ResponseEntity.ok(comments);
    }

//    // comment without condition
    @GetMapping("/latest-comments")
    public ResponseEntity<List<BookCommentDto>> getLatestComments(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<BookCommentDto> comments = bookPageService.getLatestComments(offset, limit);
        return ResponseEntity.ok(comments);
    }

    // quotes
    // quotes by bookId
    @GetMapping("/switchToQuote")
    public ResponseEntity<List<QuoteDto>> getQuotesByBookId(@RequestParam Long bookId,
                                                            @RequestParam(defaultValue = "0") int offset,
                                                            @RequestParam(defaultValue = "50") int limit) {
        List<QuoteDto> quotes = quoteService.getQuotesByBookId(bookId, offset, limit);
        return ResponseEntity.ok(quotes);
    }

    // quotes by sub-category
    @GetMapping("/latest-quotes-by-subCategory")
    public ResponseEntity<List<QuoteDto>> getLatestQuotesBySubCategory(@RequestParam String subCategory,
                                                                       @RequestParam(defaultValue = "0") int offset,
                                                                       @RequestParam(defaultValue = "50") int limit) {
        List<QuoteDto> quotes = quoteService.getQuotesBySubCategory(subCategory,offset, limit);
        return ResponseEntity.ok(quotes);
    }

    // quotes without condition
    @GetMapping("/latest-quotes")
    public ResponseEntity<List<QuoteDto>> getLatestQuotes(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<QuoteDto> quotes = quoteService.getQuotesWithoutCondition(offset, limit);
        return ResponseEntity.ok(quotes);
    }

    // detail of books by bookId
    @GetMapping("/getBookInfo")
    public ResponseEntity<BookInfoDto> getBookInfoById(@RequestParam Long bookId) {
        BookInfoDto bookInfoDto = bookPageService.getBookInfoById(bookId);
        return ResponseEntity.ok(bookInfoDto);
    }

    @GetMapping("/getAutocomplete")
    public ResponseEntity<List<BookInfoDto>> getAutoCompleteBooks(@RequestParam String keyword) {
        List<BookInfoDto> books = bookPageService.searchBooksByKeyword(keyword);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/searchBooks")
    public ResponseEntity<Page<BookInfoDto>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        Page<BookInfoDto> books = bookPageService.searchBooksByKeywordPaged(keyword, page, size);
        return ResponseEntity.ok(books);
    }
}
