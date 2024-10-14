package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookCommentDtoConverter;
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookInfoDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookCommentSqlService {

    private static final Logger logger = LoggerFactory.getLogger(BookCommentSqlService.class);
    private static final String CACHE_KEY = "latest_comments";
    private static final String LIKED_BOOKS_CACHE_KEY = "latest_liked_books";
    private static final int CACHE_SIZE = 50;

    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final BookInfoRepository bookinfoRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteService quoteService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BookCommentSqlService(BookCommentSqlRepository bookCommentSqlRepository, JwtTokenUtil jwtTokenUtil, BookInfoRepository bookInfoRepository, QuoteRepository quoteRepository, UserBookshelfSqlRepository userBookshelfSqlRepository, QuoteService quoteService, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.bookinfoRepository = bookInfoRepository;
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<BookCommentDto> getLatestComments(int offset, int limit) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        long totalCached = zSetOps.size(CACHE_KEY);

        logger.info("Attempting to fetch comments from cache. Offset: {}, Limit: {}, Total cached: {}", offset, limit, totalCached);

        if (totalCached < CACHE_SIZE || offset + limit > totalCached) {
            logger.info("Cache miss or insufficient data. Updating cache from database.");
            updateFullCache();
        }

        // 獲取緩存中的評論
        Set<String> cachedComments = zSetOps.reverseRange(CACHE_KEY, offset, offset + limit - 1);

        if (cachedComments != null && cachedComments.size() == limit) {
            logger.info("Successfully retrieved {} comments from cache", cachedComments.size());
            return deserializeComments(new ArrayList<>(cachedComments));
        } else {
            logger.warn("Comment Cache retrieval failed or incomplete. Fetching from database.");
            return getCommentsFromDatabase(offset, limit);
        }
    }

    private List<BookCommentDto> getCommentsFromDatabase(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("timestamp").descending());
        Page<BookCommentSql> commentPage = bookCommentSqlRepository.findAll(pageable);
        return BookCommentDtoConverter.convertToDtoList(commentPage.getContent());
    }

    private void updateFullCache() {
        logger.info("Updating full cache with latest comments");
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        List<BookCommentSql> latestComments = bookCommentSqlRepository.findAll(
                PageRequest.of(0, CACHE_SIZE, Sort.by("timestamp").descending())
        ).getContent();

        redisTemplate.delete(CACHE_KEY);
        logger.info("Cleared existing cache");

        for (BookCommentSql comment : latestComments) {
            zSetOps.add(CACHE_KEY, serializeComment(comment), comment.getTimestamp().getTime());
        }
        logger.info("Added {} comments to cache", latestComments.size());
    }

    private String serializeComment(BookCommentSql comment) {
        try {
            return objectMapper.writeValueAsString(BookCommentDtoConverter.convertToDto(comment));
        } catch (Exception e) {
            logger.error("Error serializing comment", e);
            throw new RuntimeException("Error serializing comment", e);
        }
    }

    private List<BookCommentDto> deserializeComments(List<String> serializedComments) {
        return serializedComments.stream()
                .map(this::deserializeComment)
                .collect(Collectors.toList());
    }

    private BookCommentDto deserializeComment(String serialized) {
        try {
            return objectMapper.readValue(serialized, BookCommentDto.class);
        } catch (IOException e) {
            logger.error("Error deserializing comment", e);
            throw new RuntimeException("Error deserializing comment", e);
        }
    }

    public void updateCache(BookCommentSql newComment) {
        logger.info("Updating cache with new comment");
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        long cacheSize = zSetOps.size(CACHE_KEY);

        if (cacheSize < CACHE_SIZE) { // 假設我們想要緩存最新的 200 條評論
            zSetOps.add(CACHE_KEY, serializeComment(newComment), newComment.getTimestamp().getTime());
            logger.info("Added new comment to cache. Current cache size: {}", cacheSize + 1);
        } else {
            Double lowestScore = zSetOps.score(CACHE_KEY, zSetOps.range(CACHE_KEY, 0, 0).iterator().next());
            if (newComment.getTimestamp().getTime() > lowestScore) {
                zSetOps.removeRange(CACHE_KEY, 0, 0);
                zSetOps.add(CACHE_KEY, serializeComment(newComment), newComment.getTimestamp().getTime());
                logger.info("Replaced oldest comment in cache with new comment");
            }
            else {
                logger.info("New comment is older than cached comments, not added to cache");
            }
        }
    }

    public boolean deleteComment(Long id, String token) {
        User user = jwtTokenUtil.getUserFromToken(token);

        // 查找該評論
        Optional<BookCommentSql> commentOpt = bookCommentSqlRepository.findById(id);
        if (commentOpt.isPresent()) {
            BookCommentSql comment = commentOpt.get();

            // 驗證該評論是否屬於當前用戶
            if (comment.getUserId().getUserId().equals(user.getUserId())) {
                bookCommentSqlRepository.delete(comment);

                // 從緩存中刪除
                ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
                Long removed = zSetOps.remove(CACHE_KEY, serializeComment(comment));
                logger.info("Removed {} entries from cache", removed);

                return true;  // 刪除成功
            }
        }
        return false;  // 刪除失敗，因為評論不存在或者權限不足
    }

    @Transactional
    public boolean addOrUpdateComment(Long bookId, String token, CommentForm commentForm) {
        try {
            logger.info("Adding new comment for book ID: {}", bookId);

            // get user from token
            User user = jwtTokenUtil.getUserFromToken(token);

            // find BookInfo
            BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

            // check if user already commented on the book
            Optional<BookCommentSql> existingComment = bookCommentSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

            // if already commented, return false
            if (existingComment.isPresent()) {
                logger.info("User {} has already commented on book {}. Comment not added.", user.getUserId(), bookId);
                return false;
            }

            // if not yet commented, set bookComment
            BookCommentSql bookCommentSql = new BookCommentSql();
            bookCommentSql.setBookId(bookInfo);
            bookCommentSql.setUserId(user);
            bookCommentSql.setComment(commentForm.getComment());
            bookCommentSql.setTimestamp(Timestamp.from(Instant.now()));
            bookCommentSql.setMainCategory(bookInfo.getMainCategory());
            bookCommentSql.setSubCategory(bookInfo.getSubCategory());

            // save comment
            bookCommentSql = bookCommentSqlRepository.save(bookCommentSql);
            logger.info("Comment saved to database with ID: {}", bookCommentSql.getId());

            // update cache
            try {
                updateCache(bookCommentSql);
                logger.info("Cache updated successfully for comment ID: {}", bookCommentSql.getId());
            } catch (RedisConnectionFailureException e) {
                logger.error("Failed to update Redis cache for comment ID: {}. Error: {}", bookCommentSql.getId(), e.getMessage());
                // 不要因為 Redis 錯誤而回滾事務
            } catch (Exception e) {
                logger.error("Unexpected error when updating cache for comment ID: {}. Error: {}", bookCommentSql.getId(), e.getMessage());
                // 不要因為緩存錯誤而回滾事務
            }

            return true;
        } catch (ResourceNotFoundException e) {
            logger.error("Book not found with id: {}. Error: {}", bookId, e.getMessage());
            throw e; // 重新拋出異常，因為這是一個嚴重的錯誤
        } catch (Exception e) {
            logger.error("Error adding new comment for book ID: {}. Error: {}", bookId, e.getMessage());
            throw new RuntimeException("Failed to add new comment", e);
        }
    }

    @Transactional
    public void addQuote(Long bookId, String token, QuoteForm quoteForm) {

        quoteService.addQuote(bookId, token, quoteForm);
    }

//    // no cache
//    @Transactional
//    public boolean likeBook(Long bookId, String token) {
//
//        // get user form token
//        User user = jwtTokenUtil.getUserFromToken(token);
//
//        // find BookInfo
//        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
//                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
//
//        // check if user already has a record in UserBookshelfSql
//        Optional<UserBookshelfSql> existingRecord = userBookshelfSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());
//
//        if (existingRecord.isPresent()) {
//            // If record exists, check the current 'likes' status
//            UserBookshelfSql userBookshelfSql = existingRecord.get();
//
//            if (Boolean.TRUE.equals(userBookshelfSql.getLikes())) {
//                // If currently liked, cancel the like
//                userBookshelfSql.setLikes(false);
//                // Set timestamp_like to null when canceling the like
//                userBookshelfSql.setTimestampLike(null);
//
//                // Decrease book's likes count
//                if (bookInfo.getLikes() != null && bookInfo.getLikes() > 0) {
//                    bookInfo.setLikes(bookInfo.getLikes() - 1);
//                }
//            } else {
//                // If not liked (or previously canceled), add a like
//                userBookshelfSql.setLikes(true);
//                userBookshelfSql.setTimestampLike(Timestamp.from(Instant.now()));
//
//                // Increase book's likes count
//                if (bookInfo.getLikes() == null) {
//                    bookInfo.setLikes(1); // if no likes yet, set to 1
//                } else {
//                    bookInfo.setLikes(bookInfo.getLikes() + 1);
//                }
//            }
//
//            // Save the updated record and bookInfo
//            userBookshelfSqlRepository.save(userBookshelfSql);
//            bookinfoRepository.save(bookInfo);
//
//            return Boolean.TRUE.equals(userBookshelfSql.getLikes()); // return true if liked, false if canceled
//        } else {
//            // If no record exists, create a new like
//            UserBookshelfSql userBookshelfSql = new UserBookshelfSql();
//            userBookshelfSql.setBookId(bookInfo);
//            userBookshelfSql.setUserId(user);
//            userBookshelfSql.setLikes(true);
//            userBookshelfSql.setCollect(false);
//            userBookshelfSql.setTimestampLike(Timestamp.from(Instant.now()));// set default collect status
//            userBookshelfSql.setMainCategory(bookInfo.getMainCategory());
//            userBookshelfSql.setSubCategory(bookInfo.getSubCategory());
//
//            // Save like
//            userBookshelfSqlRepository.save(userBookshelfSql);
//
//            // Increase book's likes count
//            if (bookInfo.getLikes() == null) {
//                bookInfo.setLikes(1); // if no likes yet, set to 1
//            } else {
//                bookInfo.setLikes(bookInfo.getLikes() + 1);
//            }
//
//            // Save the updated bookInfo
//            bookinfoRepository.save(bookInfo);
//
//            return true; // return true to indicate like was added
//        }
//    }

    @Transactional
    public boolean likeBook(Long bookId, String token) {
        User user = jwtTokenUtil.getUserFromToken(token);
        BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + bookId + " 的書籍"));

        Optional<UserBookshelfSql> existingRecord = userBookshelfSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

        UserBookshelfSql userBookshelfSql;
        boolean isLiked;

        if (existingRecord.isPresent()) {
            userBookshelfSql = existingRecord.get();
            isLiked = !Boolean.TRUE.equals(userBookshelfSql.getLikes());
            userBookshelfSql.setLikes(isLiked);
            userBookshelfSql.setTimestampLike(isLiked ? Timestamp.from(Instant.now()) : null);

            if (isLiked) {
                bookInfo.setLikes(bookInfo.getLikes() == null ? 1 : bookInfo.getLikes() + 1);
            } else if (bookInfo.getLikes() != null && bookInfo.getLikes() > 0) {
                bookInfo.setLikes(bookInfo.getLikes() - 1);
            }
        } else {
            userBookshelfSql = new UserBookshelfSql();
            userBookshelfSql.setBookId(bookInfo);
            userBookshelfSql.setUserId(user);
            userBookshelfSql.setLikes(true);
            userBookshelfSql.setCollect(false);
            userBookshelfSql.setTimestampLike(Timestamp.from(Instant.now()));
            userBookshelfSql.setMainCategory(bookInfo.getMainCategory());
            userBookshelfSql.setSubCategory(bookInfo.getSubCategory());
            isLiked = true;

            bookInfo.setLikes(bookInfo.getLikes() == null ? 1 : bookInfo.getLikes() + 1);
        }

        userBookshelfSqlRepository.save(userBookshelfSql);
        bookinfoRepository.save(bookInfo);

//        updateLikedBooksCache(bookInfo, isLiked, userBookshelfSql.getTimestampLike());

        // 更新緩存
        if (isLiked) {
            addToLikedBooksCache(bookInfo, userBookshelfSql.getTimestampLike());
        } else {
            removeFromLikedBooksCache(bookInfo);
        }

        return isLiked;
    }

    public void addToLikedBooksCache(BookInfo book, Timestamp likeTimestamp) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(LIKED_BOOKS_CACHE_KEY, serializeBook(book), likeTimestamp.getTime());
        // 如果緩存大小超過限制，移除最舊的元素
        if (zSetOps.size(LIKED_BOOKS_CACHE_KEY) > CACHE_SIZE) {
            zSetOps.removeRange(LIKED_BOOKS_CACHE_KEY, 0, 0);
        }
    }

    public void removeFromLikedBooksCache(BookInfo book) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.remove(LIKED_BOOKS_CACHE_KEY, serializeBook(book));
    }

    private void updateLikedBooksCache(BookInfo book, boolean isLiked, Timestamp timestamp) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        String serializedBook = serializeBook(book);

        if (isLiked) {
            zSetOps.add(LIKED_BOOKS_CACHE_KEY, serializedBook, timestamp.getTime());
            if (zSetOps.size(LIKED_BOOKS_CACHE_KEY) > CACHE_SIZE) {
                zSetOps.removeRange(LIKED_BOOKS_CACHE_KEY, 0, 0);
            }
        } else {
            zSetOps.remove(LIKED_BOOKS_CACHE_KEY, serializedBook);
        }

        logger.info("已更新喜歡的書籍緩存。書籍 ID: {}, 是否喜歡: {}", book.getBookId(), isLiked);
    }

    private String serializeBook(BookInfo book) {
        try {
            return objectMapper.writeValueAsString(BookInfoDtoConverter.convertToDto(book));
        } catch (Exception e) {
            logger.error("序列化書籍時發生錯誤", e);
            throw new RuntimeException("序列化書籍失敗", e);
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

    public boolean editComment(Long id, String token, String updatedComment) {
        try {
            User user = jwtTokenUtil.getUserFromToken(token);
            Optional<BookCommentSql> commentOpt = bookCommentSqlRepository.findById(id);

            if (commentOpt.isPresent() && commentOpt.get().getUserId().getUserId().equals(user.getUserId())) {
                BookCommentSql commentToUpdate = commentOpt.get();
                commentToUpdate.setComment(updatedComment);

                // 更新資料庫
                bookCommentSqlRepository.save(commentToUpdate);

                // 更新緩存
                updateCache(commentToUpdate);

                logger.info("Comment updated successfully. ID: {}", id);
                return true;
            } else {
                logger.warn("Failed to edit comment. ID: {}. Comment not found or unauthorized.", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error editing comment with ID: {}", id, e);
            return false;
        }
    }
}

