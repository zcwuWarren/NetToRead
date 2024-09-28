package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookCommentDtoConverter;
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookInfoDtoConverter;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class BookPageService {

    private final BookInfoRepository bookInfoRepository;
    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;



    public BookPageService(BookInfoRepository bookInfoRepository, BookCommentSqlRepository bookCommentSqlRepository, UserBookshelfSqlRepository userBookshelfSqlRepository) {
        this.bookInfoRepository = bookInfoRepository;
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
    }

    public List<Map<String, Object>> getCategories() {
        return bookInfoRepository.findDistinctCategories();
    }

    public List<BookInfoDto> getTop6BooksByLikesByCategory(String subCategory) {
        List<BookInfo> books = bookInfoRepository.findTop6BySubCategoryOrderByLikesDesc(subCategory);
        // turn to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookCommentDto> getLatestCommentsBySubCategory(String subCategory) {

        List<BookCommentSql> comments = bookCommentSqlRepository.findBySubCategoryOrderByTimestampDesc(subCategory);
        return BookCommentDtoConverter.convertToDtoList(comments);
    }

    public BookInfoDto getBookInfoById(Long bookId) {
        BookInfo book = bookInfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return BookInfoDtoConverter.convertToDto(book);
    }

    public List<BookCommentDto> getLatestComments() {
        List<BookCommentSql> comments = bookCommentSqlRepository.findTop6ByOrderByTimestampDesc();
        return BookCommentDtoConverter.convertToDtoList(comments);
    }

    public List<BookInfoDto> getLatestCollectBooksByCategory(String subCategory) {
//        List<BookInfo> books = bookInfoRepository.findTop6BySubCategoryOrderByCollectDesc(subCategory);
        List<Long> bookIds = userBookshelfSqlRepository.findTop6BookIdsBySubCategoryOrderByTimestampCollectDesc(subCategory);
        System.out.println(bookIds.toString());
        // get book data from bookinfo by bookIds
        List<BookInfo> books = bookInfoRepository.findByBookIdIn(bookIds);
        System.out.println(books);

        // manually sort the list order
        books.sort(Comparator.comparingInt(book -> bookIds.indexOf(book.getBookId())));

        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookInfoDto> getLatestLikedBooksByCategory(String subCategory) {

        // get bookId from user_bookshelf by subCategory and timestampLike
        List<Long> bookIds = userBookshelfSqlRepository.findTop6BookIdsBySubCategoryOrderByTimestampLikeDesc(subCategory);
        System.out.println(bookIds.toString());
        // get book data from bookinfo by bookIds
        List<BookInfo> books = bookInfoRepository.findByBookIdIn(bookIds);

        // manually sort the list order
        books.sort(Comparator.comparingInt(book -> bookIds.indexOf(book.getBookId())));

        // turn to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookInfoDto> getLatestLikedBooks() {

        // get bookId from user_bookshelf by timestampLike
        List<Long> bookIds = userBookshelfSqlRepository.findTop6BookIdsByOrderByTimestampLikeDesc();
        System.out.println(bookIds.toString());
        List<BookInfo> books = bookInfoRepository.findByBookIdIn(bookIds);

        // manually sort the list order
        books.sort(Comparator.comparingInt(book -> bookIds.indexOf(book.getBookId())));
        // turn to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookInfoDto> getLatestCollectBooks() {
        // get bookId from user_bookshelf by subCategory and timestampLike
        List<Long> bookIds = userBookshelfSqlRepository.findTop6BookIdsOrderByTimestampCollectDesc();
        System.out.println(bookIds.toString());
        List<BookInfo> books = bookInfoRepository.findByBookIdIn(bookIds);

        // manually sort the list order
        books.sort(Comparator.comparingInt(book -> bookIds.indexOf(book.getBookId())));
        // turn to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookInfoDto> searchBooksByKeyword(String keyword) {
        List<BookInfo> books = bookInfoRepository.findByBookNameContaining(keyword);
        return BookInfoDtoConverter.convertToDtoList(books);
    }
}
