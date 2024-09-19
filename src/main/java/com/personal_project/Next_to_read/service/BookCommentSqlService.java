package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.form.CommentForm;
import com.personal_project.Next_to_read.data.form.QuoteForm;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookCommentSqlService {

    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlService userBookshelfSqlService;
    private final JwtTokenUtil jwtTokenUtil;
    private final BookInfoRepository bookinfoRepository;
    private final QuoteRepository quoteRepository;

    public BookCommentSqlService(BookCommentSqlRepository bookCommentSqlRepository, UserBookshelfSqlService userBookshelfSqlService, JwtTokenUtil jwtTokenUtil, BookInfoRepository bookInfoRepository, QuoteRepository quoteRepository) {
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlService = userBookshelfSqlService;
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

        // 檢查是否已經存在評論
        Optional<BookCommentSql> existingComment = bookCommentSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        if (existingComment.isPresent()) {
            // 已有評論，返回 false，表示失敗
            return false;
        }

        // set bookComment
        BookCommentSql bookCommentSql = new BookCommentSql();
        bookCommentSql.setBookId(bookInfo);
        bookCommentSql.setUserId(user);
        bookCommentSql.setComment(commentForm.getComment());

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
        quote.setBook(bookInfo);
        quote.setUser(user);
        quote.setQuoteText(quoteForm.getQuote());

        // save quote
        quoteRepository.save(quote);
    }
}

