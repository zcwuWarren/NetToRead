package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.data.form.CommentForm;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookPageService {

    private static final Logger logger = LoggerFactory.getLogger(BookPageService.class);
    private static final String CACHE_KEY = "latest_comments";
    private static final int CACHE_SIZE = 200;

    private final BookInfoRepository bookInfoRepository;
    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BookPageService(BookInfoRepository bookInfoRepository, BookCommentSqlRepository bookCommentSqlRepository, UserBookshelfSqlRepository userBookshelfSqlRepository, JwtTokenUtil jwtTokenUtil, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.bookInfoRepository = bookInfoRepository;
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> getCategories() {
        return bookInfoRepository.findDistinctCategories();
    }

    public List<BookInfoDto> getTop6BooksByLikesByCategory(String subCategory) {
        List<BookInfo> books = bookInfoRepository.findTop6BySubCategoryOrderByLikesDesc(subCategory);
        // turn to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    public List<BookCommentDto> getCommentsByBookId(Long bookId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<BookCommentSql> commentPage = bookCommentSqlRepository.findByBookIdOrderByTimestampDesc(bookId, pageable);
        return BookCommentDtoConverter.convertToDtoList(commentPage.getContent());
    }

    public List<BookCommentDto> getCommentsBySubCategory(String subCategory, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<BookCommentSql> commentPage = bookCommentSqlRepository.findBySubCategoryOrderByTimestampDesc(subCategory, pageable);
        return BookCommentDtoConverter.convertToDtoList(commentPage.getContent());
    }

    public List<BookCommentDto> getCommentsByUserId(String token, int offset, int limit) {
        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<BookCommentSql> commentPage = bookCommentSqlRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return BookCommentDtoConverter.convertToDtoList(commentPage.getContent());
    }


    public BookInfoDto getBookInfoById(Long bookId) {
        BookInfo book = bookInfoRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return BookInfoDtoConverter.convertToDto(book);
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

//    private String serializeComment(BookCommentSql comment) {
//        try {
//            ObjectNode node = objectMapper.createObjectNode()
//                    .put("id", comment.getId())
//                    .put("comment", comment.getComment())
//                    .put("timestamp", comment.getTimestamp().getTime())
//                    .put("mainCategory", comment.getMainCategory())
//                    .put("subCategory", comment.getSubCategory())
//                    .put("userId", comment.getUserId().getUserId())
//                    .put("userName", comment.getUserId().getName())
//                    .put("bookId", comment.getBookId().getBookId())
//                    .put("bookName", comment.getBookId().getBookName());
//
//            return objectMapper.writeValueAsString(node);
//        } catch (Exception e) {
//            logger.error("Error serializing comment", e);
//            throw new RuntimeException("Error serializing comment", e);
//        }
//    }

    private List<BookCommentDto> deserializeComments(List<String> serializedComments) {
        return serializedComments.stream()
                .map(this::deserializeComment)
                .collect(Collectors.toList());
    }

//    private List<BookCommentDto> deserializeComments(List<String> serializedComments) {
//        return serializedComments.stream()
//                .map(this::deserializeComment)
//                .map(this::convertToBookCommentDto)
//                .collect(Collectors.toList());
//    }

    private BookCommentDto deserializeComment(String serialized) {
        try {
            return objectMapper.readValue(serialized, BookCommentDto.class);
        } catch (IOException e) {
            logger.error("Error deserializing comment", e);
            throw new RuntimeException("Error deserializing comment", e);
        }
    }

//    private BookCommentSql deserializeComment(String serialized) {
//        try {
//            ObjectNode node = (ObjectNode) objectMapper.readTree(serialized);
//
//            BookCommentSql comment = new BookCommentSql();
//            comment.setId(node.get("id").asLong());
//            comment.setComment(node.get("comment").asText());
//            comment.setTimestamp(new Timestamp(node.get("timestamp").asLong()));
//            comment.setMainCategory(node.get("mainCategory").asText());
//            comment.setSubCategory(node.get("subCategory").asText());
//
//            User user = new User();
//            user.setUserId(node.get("userId").asLong());
//            user.setName(node.get("userName").asText());
//            comment.setUserId(user);
//
//            BookInfo bookInfo = new BookInfo();
//            bookInfo.setBookId(node.get("bookId").asLong());
//            bookInfo.setBookName(node.get("bookName").asText());
//            comment.setBookId(bookInfo);
//
//            return comment;
//        } catch (IOException e) {
//            logger.error("Error deserializing comment", e);
//            throw new RuntimeException("Error deserializing comment", e);
//        }
//    }

//    private BookCommentDto convertToBookCommentDto(BookCommentSql comment) {
//        return new BookCommentDto(
//                comment.getUserId().getUserId(),
//                comment.getComment(),
//                comment.getBookId().getBookName(),
//                DateUtil.formatDate(comment.getTimestamp()),
//                comment.getBookId().getBookId(),
//                comment.getUserId().getName(),
//                comment.getId()
//        );
//    }

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

    public List<BookInfoDto> getLatestCollectBooksByCategory(String subCategory, int offset ,int limit) {
// Get bookIds from user_bookshelf by timestampLike with pagination
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Long> bookIdPage = userBookshelfSqlRepository.findBookIdsBySubCategoryOrderByTimestampCollectDesc(subCategory, pageable);
//        System.out.println(bookIdPage.toString());
        List<Long> bookIds = bookIdPage.getContent();
//        System.out.println(bookIds);

        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch books and maintain the order
        Map<Long, BookInfo> bookMap = bookInfoRepository.findByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getBookId, Function.identity()));

        // Manually sort the list to maintain the order from bookIds
        List<BookInfo> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Convert to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(sortedBooks);
    }

    public List<BookInfoDto> getLatestLikedBooksByCategory(String subCategory, int offset, int limit) {
        // Get bookIds from user_bookshelf by timestampLike with pagination
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Long> bookIdPage = userBookshelfSqlRepository.findBookIdsBySubCategoryOrderByTimestampLikeDesc(subCategory, pageable);
//        System.out.println(bookIdPage.toString());
        List<Long> bookIds = bookIdPage.getContent();
//        System.out.println(bookIds);

        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch books and maintain the order
        Map<Long, BookInfo> bookMap = bookInfoRepository.findByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getBookId, Function.identity()));

        // Manually sort the list to maintain the order from bookIds
        List<BookInfo> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Convert to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(sortedBooks);
    }

    public List<BookInfoDto> getLatestLikedBooks(int offset, int limit) {
        // Get bookIds from user_bookshelf by timestampLike with pagination
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Long> bookIdPage = userBookshelfSqlRepository.findBookIdsByOrderByTimestampLikeDesc(pageable);
//        System.out.println(bookIdPage.toString());
        List<Long> bookIds = bookIdPage.getContent();
//        System.out.println(bookIds);

        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch books and maintain the order
        Map<Long, BookInfo> bookMap = bookInfoRepository.findByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getBookId, Function.identity()));

        // Manually sort the list to maintain the order from bookIds
        List<BookInfo> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Convert to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(sortedBooks);
    }

    public List<BookInfoDto> getLatestCollectBooks(int offset, int limit) {
        // Get bookIds from user_bookshelf by timestampLike with pagination
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Long> bookIdPage = userBookshelfSqlRepository.findBookIdsByOrderByTimestampCollectDesc(pageable);
//        System.out.println(bookIdPage.toString());
        List<Long> bookIds = bookIdPage.getContent();
//        System.out.println(bookIds);

        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch books and maintain the order
        Map<Long, BookInfo> bookMap = bookInfoRepository.findByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getBookId, Function.identity()));

        // Manually sort the list to maintain the order from bookIds
        List<BookInfo> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Convert to BookInfoDto
        return BookInfoDtoConverter.convertToDtoList(sortedBooks);
    }

    // searchbar autocomplete
    public List<BookInfoDto> searchBooksByKeyword(String keyword) {
        List<BookInfo> books = bookInfoRepository.findByBookNameContaining(keyword);
        return BookInfoDtoConverter.convertToDtoList(books);
    }

    // search book
    public Page<BookInfoDto> searchBooksByKeywordPaged(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookInfo> booksPage = bookInfoRepository.findByBookNameContainingIgnoreCase(keyword, pageable);
        return booksPage.map(BookInfoDtoConverter::convertToDto);
    }
}
