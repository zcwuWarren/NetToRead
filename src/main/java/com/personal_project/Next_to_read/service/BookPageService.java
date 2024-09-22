package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookPageService {

    private final BookInfoRepository bookInfoRepository;
    private final BookCommentSqlRepository bookCommentSqlRepository;


    public BookPageService(BookInfoRepository bookInfoRepository, BookCommentSqlRepository bookCommentSqlRepository) {
        this.bookInfoRepository = bookInfoRepository;
        this.bookCommentSqlRepository = bookCommentSqlRepository;
    }

    public List<Map<String, Object>> getCategories() {
        return bookInfoRepository.findDistinctCategories();
    }

    public List<BookInfoDto> getTop6BooksByLikesByCategory(String subCategory) {
        List<BookInfo> books = bookInfoRepository.findTop6BySubCategoryOrderByLikesDesc(subCategory);

        // turn to BookInfoDto
        return books.stream()
                .map(book -> BookInfoDto.builder()
                        .bookId(book.getBookId())
                        .bookName(book.getBookName())
                        .bookCover(book.getBookCover())
                        .build())
                .collect(Collectors.toList());
    }

    public List<BookInfoDto> getTop6BooksByLikes() {
        List<BookInfo> books = bookInfoRepository.findTop6ByOrderByLikesDesc();

        // turn to BookInfoDto
        return books.stream()
                .map(book -> BookInfoDto.builder()
                        .bookId(book.getBookId())
                        .bookName(book.getBookName())
                        .bookCover(book.getBookCover())
                        .build())
                .collect(Collectors.toList());

    }

    public List<BookCommentDto> getLatestCommentsBySubCategory(String subCategory) {

        List<BookCommentSql> comments = bookCommentSqlRepository.findBySubCategoryOrderByTimestampDesc(subCategory);
        return comments.stream()
                .map(comment -> BookCommentDto.builder()
                        .userId(comment.getUserId().getUserId())
                        .comment(comment.getComment())
                        .bookName(comment.getBookId().getBookName())
                        .date(DateUtil.formatDate(comment.getTimestamp()))
                        .build())
                .collect(Collectors.toList());
    }

    public BookInfoDto getBookInfoById(Long bookId) {
        BookInfo book = bookInfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return BookInfoDto.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .author(book.getAuthor())
                .like(book.getLikes())
                .collect(book.getCollect())
                .content(book.getContent())
                .description(book.getDescription())
                .bookName(book.getBookName())
                .bookCover(book.getBookCover())
                .build();
    }
}
