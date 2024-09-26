package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.form.CommentForm;
import com.personal_project.Next_to_read.data.form.QuoteForm;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.*;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookCommentSqlService {

    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final BookInfoRepository bookinfoRepository;
    private final QuoteRepository quoteRepository;

    public BookCommentSqlService(BookCommentSqlRepository bookCommentSqlRepository, JwtTokenUtil jwtTokenUtil, BookInfoRepository bookInfoRepository, QuoteRepository quoteRepository, UserBookshelfSqlRepository userBookshelfSqlRepository) {
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.bookinfoRepository = bookInfoRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional
    public boolean addOrUpdateComment(Long bookId, String token, CommentForm commentForm) {

        // get user form token
        User user = jwtTokenUtil.getUserFromToken(token);

        // find BookInfo
        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // check if user already commented to the book
        Optional<BookCommentSql> existingComment = bookCommentSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        // if already commented, return false
        if (existingComment.isPresent()) {
            return false;
        }

        // if not yet comment, set bookComment
        BookCommentSql bookCommentSql = new BookCommentSql();
        bookCommentSql.setBookId(bookInfo);
        bookCommentSql.setUserId(user);
        bookCommentSql.setComment(commentForm.getComment());
        bookCommentSql.setTimestamp(Timestamp.from(Instant.now()));
        bookCommentSql.setMainCategory(bookInfo.getMainCategory());
        bookCommentSql.setSubCategory(bookInfo.getSubCategory());

        // save comment
        bookCommentSqlRepository.save(bookCommentSql);
        return true;
    }


    @Transactional
    public void addQuote(Long bookId, String token, QuoteForm quoteForm) {

        // get user form token
        User user = jwtTokenUtil.getUserFromToken(token);

        // find BookInfo
        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // set quote
        Quote quote = new Quote();
        quote.setBookId(bookInfo);
        quote.setUserId(user);
        quote.setQuote(quoteForm.getQuote());
        quote.setTimestamp(Timestamp.from(Instant.now()));
        quote.setMainCategory(bookInfo.getMainCategory());
        quote.setSubCategory(bookInfo.getSubCategory());

        // save quote
        quoteRepository.save(quote);
    }

    @Transactional
    public boolean likeBook(Long bookId, String token) {

        // get user form token
        User user = jwtTokenUtil.getUserFromToken(token);

        // find BookInfo
        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // check if user already has a record in UserBookshelfSql
        Optional<UserBookshelfSql> existingRecord = userBookshelfSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        if (existingRecord.isPresent()) {
            // If record exists, check the current 'likes' status
            UserBookshelfSql userBookshelfSql = existingRecord.get();

            if (Boolean.TRUE.equals(userBookshelfSql.getLikes())) {
                // If currently liked, cancel the like
                userBookshelfSql.setLikes(false);
                // Set timestamp_like to null when canceling the like
                userBookshelfSql.setTimestampLike(null);

                // Decrease book's likes count
                if (bookInfo.getLikes() != null && bookInfo.getLikes() > 0) {
                    bookInfo.setLikes(bookInfo.getLikes() - 1);
                }
            } else {
                // If not liked (or previously canceled), add a like
                userBookshelfSql.setLikes(true);
                userBookshelfSql.setTimestampLike(Timestamp.from(Instant.now()));

                // Increase book's likes count
                if (bookInfo.getLikes() == null) {
                    bookInfo.setLikes(1); // if no likes yet, set to 1
                } else {
                    bookInfo.setLikes(bookInfo.getLikes() + 1);
                }
            }

            // Save the updated record and bookInfo
            userBookshelfSqlRepository.save(userBookshelfSql);
            bookinfoRepository.save(bookInfo);

            return Boolean.TRUE.equals(userBookshelfSql.getLikes()); // return true if liked, false if canceled
        } else {
            // If no record exists, create a new like
            UserBookshelfSql userBookshelfSql = new UserBookshelfSql();
            userBookshelfSql.setBookId(bookInfo);
            userBookshelfSql.setUserId(user);
            userBookshelfSql.setLikes(true);
            userBookshelfSql.setCollect(false);
            userBookshelfSql.setTimestampLike(Timestamp.from(Instant.now()));// set default collect status
            userBookshelfSql.setMainCategory(bookInfo.getMainCategory());
            userBookshelfSql.setSubCategory(bookInfo.getSubCategory());

            // Save like
            userBookshelfSqlRepository.save(userBookshelfSql);

            // Increase book's likes count
            if (bookInfo.getLikes() == null) {
                bookInfo.setLikes(1); // if no likes yet, set to 1
            } else {
                bookInfo.setLikes(bookInfo.getLikes() + 1);
            }

            // Save the updated bookInfo
            bookinfoRepository.save(bookInfo);

            return true; // return true to indicate like was added
        }
    }

    @Transactional
    public boolean collectBook(Long bookId, String token) {

        // get user form token
        User user = jwtTokenUtil.getUserFromToken(token);

        // find BookInfo
        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // check if user already collect to the book
        Optional<UserBookshelfSql> existingCollect = userBookshelfSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        // check if user already has a record in UserBookshelfSql
        Optional<UserBookshelfSql> existingRecord = userBookshelfSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        if (existingRecord.isPresent()) {
            // If record exists, check the current 'collect' status
            UserBookshelfSql userBookshelfSql = existingRecord.get();

            if (Boolean.TRUE.equals(userBookshelfSql.getCollect())) {
                // If currently collected, cancel the collect
                userBookshelfSql.setCollect(false);
                // Set timestamp_collect to null when canceling the collect
                userBookshelfSql.setTimestampCollect(null);

                // Decrease book's collect count
                if (bookInfo.getCollect() != null && bookInfo.getCollect() > 0) {
                    bookInfo.setCollect(bookInfo.getCollect() - 1);
                }
            } else {
                // If not collected (or previously canceled), add a collect
                userBookshelfSql.setCollect(true);
                userBookshelfSql.setTimestampCollect(Timestamp.from(Instant.now()));

                // Increase book's collect count
                if (bookInfo.getCollect() == null) {
                    bookInfo.setCollect(1); // if no collects yet, set to 1
                } else {
                    bookInfo.setCollect(bookInfo.getCollect() + 1);
                }
            }

            // Save the updated record and bookInfo
            userBookshelfSqlRepository.save(userBookshelfSql);
            bookinfoRepository.save(bookInfo);

            return Boolean.TRUE.equals(userBookshelfSql.getCollect()); // return true if collected, false if canceled
        } else {
            // If no record exists, create a new collect
            UserBookshelfSql userBookshelfSql = new UserBookshelfSql();
            userBookshelfSql.setBookId(bookInfo);
            userBookshelfSql.setUserId(user);
            userBookshelfSql.setCollect(true);
            userBookshelfSql.setLikes(false); // set default likes status
            userBookshelfSql.setTimestampCollect(Timestamp.from(Instant.now()));
            userBookshelfSql.setMainCategory(bookInfo.getMainCategory());
            userBookshelfSql.setSubCategory(bookInfo.getSubCategory());

            // Save collect
            userBookshelfSqlRepository.save(userBookshelfSql);

            // Increase book's collect count
            if (bookInfo.getCollect() == null) {
                bookInfo.setCollect(1); // if no collects yet, set to 1
            } else {
                bookInfo.setCollect(bookInfo.getCollect() + 1);
            }

            // Save the updated bookInfo
            bookinfoRepository.save(bookInfo);

            return true; // return true to indicate collect was added
        }
    }

    public List<BookCommentDto> getCommentsByBookId(Long bookId) {

        List<BookCommentSql> comments = bookCommentSqlRepository.findByBookId_BookIdOrderByTimestampDesc(bookId);
        return comments.stream().map(comment -> new BookCommentDto(comment)).collect(Collectors.toList());
    }

    public List<BookCommentDto> getCommentsByUserId(String token) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        List<BookCommentSql> comments = bookCommentSqlRepository.findByUserId_UserIdOrderByTimestampDesc(userId);
        return comments.stream().map(comment -> new BookCommentDto(comment)).collect(Collectors.toList());
    }
}

