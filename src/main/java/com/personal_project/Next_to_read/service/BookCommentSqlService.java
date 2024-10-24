package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.personal_project.Next_to_read.util.DateUtil;
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
        assert limit == CACHE_SIZE : "Limit must be equal to CACHE_SIZE";

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long totalCached = zSetOps.size(CACHE_KEY);

            logger.info("Attempting to fetch comments. Offset: {}, Limit: {}, Total cached: {}", offset, limit, totalCached);

            if (offset == 0 && totalCached < CACHE_SIZE) {
                logger.info("Requesting first page and cache is not full. Updating cache from database.");
                updateFullCache();
            }

            if (offset == 0) {
                Set<String> cachedComments = zSetOps.reverseRange(CACHE_KEY, 0, CACHE_SIZE - 1);
                if (cachedComments != null && cachedComments.size() == CACHE_SIZE) {
                    logger.info("Retrieved first page ({} comments) from cache", cachedComments.size());
                    return deserializeComments(new ArrayList<>(cachedComments));
                }
            }

            logger.info("Fetching comments from database. Offset: {}, Limit: {}", offset, limit);
            return getCommentsFromDatabase(offset, limit);
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to connect to Redis. Falling back to database.", e);
            return getCommentsFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("Unexpected error when fetching comments", e);
            return getCommentsFromDatabase(offset, limit);
        }
    }

    private List<BookCommentDto> getCommentsFromDatabase(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("timestamp").descending());
        Page<BookCommentSql> commentPage = bookCommentSqlRepository.findAll(pageable);
        return BookCommentDtoConverter.convertToDtoList(commentPage.getContent());
    }

    private void updateFullCache() {
        try {
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
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to update full cache due to Redis connection issue", e);
        } catch (Exception e) {
            logger.error("Unexpected error when updating full cache", e);
        }
    }

    private String serializeComment(BookCommentSql comment) {
        try {
            ObjectNode node = objectMapper.createObjectNode()
                    .put("id", comment.getId())
                    .put("comment", comment.getComment())
                    .put("timestamp", comment.getTimestamp().getTime())
                    .put("mainCategory", comment.getMainCategory())
                    .put("subCategory", comment.getSubCategory())
                    .put("userId", comment.getUserId().getUserId())
                    .put("bookId", comment.getBookId().getBookId())
                    .put("userName", comment.getUserId().getName())
                    .put("bookName", comment.getBookId().getBookName());

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing comment", e);
        }
    }

    private List<BookCommentDto> deserializeComments(List<String> serializedComments) {
        return serializedComments.stream()
                .map(this::deserializeComment)
                .map(this::convertToCommentDto)
                .collect(Collectors.toList());
    }

    private BookCommentSql deserializeComment(String serialized) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(serialized);

            BookCommentSql comment = new BookCommentSql();
            comment.setId(node.get("id").asLong());
            comment.setComment(node.get("comment").asText());
            comment.setTimestamp(new Timestamp(node.get("timestamp").asLong()));
            comment.setMainCategory(node.get("mainCategory").asText());
            comment.setSubCategory(node.get("subCategory").asText());

            User user = new User();
            user.setUserId(node.get("userId").asLong());
            user.setName(node.get("userName").asText());
            comment.setUserId(user);

            BookInfo bookInfo = new BookInfo();
            bookInfo.setBookId(node.get("bookId").asLong());
            bookInfo.setBookName(node.get("bookName").asText());
            comment.setBookId(bookInfo);

            return comment;
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing comment", e);
        }
    }

    private BookCommentDto convertToCommentDto(BookCommentSql comment) {
        return new BookCommentDto(
                comment.getUserId().getUserId(),
                comment.getComment(),
                comment.getBookId().getBookName(),
                DateUtil.formatDate(comment.getTimestamp()),
                comment.getBookId().getBookId(),
                comment.getUserId().getName(),
                comment.getId()
        );
    }

    private void updateCache(BookCommentSql newComment) {
        try {
            logger.info("Updating cache with new comment");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long cacheSize = zSetOps.size(CACHE_KEY);

            if (cacheSize < CACHE_SIZE) {
                zSetOps.add(CACHE_KEY, serializeComment(newComment), newComment.getTimestamp().getTime());
                logger.info("Added new comment to cache. Current cache size: {}", cacheSize + 1);
            } else {
                Double oldestScore = zSetOps.score(CACHE_KEY, zSetOps.range(CACHE_KEY, 0, 0).iterator().next());
                if (newComment.getTimestamp().getTime() > oldestScore) {
                    zSetOps.removeRange(CACHE_KEY, 0, 0);
                    zSetOps.add(CACHE_KEY, serializeComment(newComment), newComment.getTimestamp().getTime());
                    logger.info("Replaced oldest comment in cache with new comment");
                } else {
                    logger.info("New comment is older than cached comments, not added to cache");
                }
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to update cache due to Redis connection issue", e);
        } catch (Exception e) {
            logger.error("Unexpected error when updating cache", e);
        }
    }

    public boolean deleteComment(Long id, String token) {
        try {
            User user = jwtTokenUtil.getUserFromToken(token);
            Optional<BookCommentSql> commentOpt = bookCommentSqlRepository.findById(id);
            if (commentOpt.isPresent()) {
                BookCommentSql comment = commentOpt.get();
                if (comment.getUserId().getUserId().equals(user.getUserId())) {
                    bookCommentSqlRepository.delete(comment);

                    // delete form cache
                    ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
                    Set<String> cachedComments = zSetOps.rangeByScore(CACHE_KEY, comment.getTimestamp().getTime(), comment.getTimestamp().getTime());
                    Long removed = 0L;
                    if (!cachedComments.isEmpty()) {
                        removed = zSetOps.remove(CACHE_KEY, cachedComments.iterator().next());
                    }
                    logger.info("Removed {} entries from cache", removed);

                    // if deleted comment in cache, getting new comment into cache
                    if (removed > 0) {
                        updateCacheAfterDeletion();
                    }

                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting comment", e);
            return false;
        }
    }

    private void updateCacheAfterDeletion() {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        long cacheSize = zSetOps.size(CACHE_KEY);
        if (cacheSize < CACHE_SIZE) {
            // get timestamp of oldest comment in cache
            Set<String> oldestCommentSet = zSetOps.range(CACHE_KEY, -1, -1);
            if (oldestCommentSet.isEmpty()) {
                logger.warn("Cache is empty after deletion. Unable to determine oldest comment.");
                return;
            }
            String oldestCommentStr = oldestCommentSet.iterator().next();
            BookCommentSql oldestComment = deserializeComment(oldestCommentStr);
            Timestamp oldestTimestamp = oldestComment.getTimestamp();

            // get next older comment by timestamp from database into cache
            List<BookCommentSql> nextComments = bookCommentSqlRepository.findFirstByTimestampLessThanOrderByTimestampDesc(
                    oldestTimestamp,
                    PageRequest.of(0, 1)
            );

            if (!nextComments.isEmpty()) {
                BookCommentSql nextComment = nextComments.get(0);
                String serializedNextComment = serializeComment(nextComment);
                zSetOps.add(CACHE_KEY, serializedNextComment, nextComment.getTimestamp().getTime());
                logger.info("Added next comment to cache after deletion. Cache size: {}", cacheSize + 1);
            } else {
                logger.info("No more comments available to add to cache after deletion.");
            }
        }
    }

    @Transactional
    public boolean addOrUpdateComment(Long bookId, String token, CommentForm commentForm) {
        try {
            logger.info("Adding new comment for book ID: {}", bookId);

            // get user for JWTtoken
            User user = jwtTokenUtil.getUserFromToken(token);

            // find BookInfo
            BookInfo bookInfo = bookinfoRepository.findByBookId(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

            // check if user has already commented the book
            Optional<BookCommentSql> existingComment = bookCommentSqlRepository.findByUserId_UserIdAndBookId_BookId(user.getUserId(), bookInfo.getBookId());

            if (existingComment.isPresent()) {
                logger.info("User {} has already commented on book {}. Comment not added.", user.getUserId(), bookId);
                return false;
            }

            // if not yet comment, add new comment
            BookCommentSql bookCommentSql = new BookCommentSql();
            bookCommentSql.setBookId(bookInfo);
            bookCommentSql.setUserId(user);
            bookCommentSql.setComment(commentForm.getComment());
            bookCommentSql.setTimestamp(Timestamp.from(Instant.now()));
            bookCommentSql.setMainCategory(bookInfo.getMainCategory());
            bookCommentSql.setSubCategory(bookInfo.getSubCategory());

            bookCommentSqlRepository.save(bookCommentSql);
            logger.info("Comment saved to database with ID: {}", bookCommentSql.getId());

            // update cache
            updateCache(bookCommentSql);

            return true;
        } catch (ResourceNotFoundException e) {
            logger.error("Book not found with id: {}. Error: {}", bookId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding new comment for book ID: {}. Error: {}", bookId, e.getMessage());
            throw new RuntimeException("Failed to add new comment", e);
        }
    }

    @Transactional
    public void addQuote(Long bookId, String token, QuoteForm quoteForm) {

        quoteService.addQuote(bookId, token, quoteForm);
    }

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

        // update cache
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
        // if cache liked book > cache size, removing the oldest member
        if (zSetOps.size(LIKED_BOOKS_CACHE_KEY) > CACHE_SIZE) {
            zSetOps.removeRange(LIKED_BOOKS_CACHE_KEY, 0, 0);
        }
    }

    public void removeFromLikedBooksCache(BookInfo book) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.remove(LIKED_BOOKS_CACHE_KEY, serializeBook(book));
    }

//    private void updateLikedBooksCache(BookInfo book, boolean isLiked, Timestamp timestamp) {
//        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
//        String serializedBook = serializeBook(book);
//
//        if (isLiked) {
//            zSetOps.add(LIKED_BOOKS_CACHE_KEY, serializedBook, timestamp.getTime());
//            if (zSetOps.size(LIKED_BOOKS_CACHE_KEY) > CACHE_SIZE) {
//                zSetOps.removeRange(LIKED_BOOKS_CACHE_KEY, 0, 0);
//            }
//        } else {
//            zSetOps.remove(LIKED_BOOKS_CACHE_KEY, serializedBook);
//        }
//
//        logger.info("已更新喜歡的書籍緩存。書籍 ID: {}, 是否喜歡: {}", book.getBookId(), isLiked);
//    }

    private String serializeBook(BookInfo book) {
        try {
            return objectMapper.writeValueAsString(BookInfoDtoConverter.convertToDto(book));
        } catch (Exception e) {
            logger.error("serializing error", e);
            throw new RuntimeException("serialized failed", e);
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
                ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

                // find Comment in cache
                Set<String> cachedComments = zSetOps.rangeByScore(CACHE_KEY, commentToUpdate.getTimestamp().getTime(), commentToUpdate.getTimestamp().getTime());
                boolean inCache = !cachedComments.isEmpty();

                // update comment content, without updating timestamp
                commentToUpdate.setComment(updatedComment);
                bookCommentSqlRepository.save(commentToUpdate);

                if (inCache) {
                    // if edited comment already in cache, update cache
                    String oldSerializedComment = cachedComments.iterator().next();
                    zSetOps.remove(CACHE_KEY, oldSerializedComment);
                    String newSerializedComment = serializeComment(commentToUpdate);
                    zSetOps.add(CACHE_KEY, newSerializedComment, commentToUpdate.getTimestamp().getTime());
                    logger.info("Updated comment in cache. ID: {}", id);
                } else {
                    logger.info("Comment not in cache, no cache update needed. ID: {}", id);
                }

                logger.info("Comment updated successfully in database. ID: {}", id);
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

