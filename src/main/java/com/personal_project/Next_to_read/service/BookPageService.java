package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.data.dto.BookInfoDto;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.model.UserBookshelfSql;
import com.personal_project.Next_to_read.repository.BookCommentSqlRepository;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.UserBookshelfSqlRepository;
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookCommentDtoConverter;
import com.personal_project.Next_to_read.util.EntityToDtoConverter.BookInfoDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookPageService {

    private static final Logger logger = LoggerFactory.getLogger(BookPageService.class);
    private static final String LIKED_BOOKS_CACHE_KEY = "latest_liked_books";
    private static final int CACHE_SIZE = 50;
    private static final String COLLECTED_BOOKS_CACHE_KEY = "latest_collected_books";

    private final BookInfoRepository bookInfoRepository;
    private final BookCommentSqlRepository bookCommentSqlRepository;
    private final UserBookshelfSqlRepository userBookshelfSqlRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final BookCommentSqlService bookCommentSqlService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BookPageService(BookInfoRepository bookInfoRepository, BookCommentSqlRepository bookCommentSqlRepository, UserBookshelfSqlRepository userBookshelfSqlRepository, JwtTokenUtil jwtTokenUtil, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper, BookCommentSqlService bookCommentSqlService) {
        this.bookInfoRepository = bookInfoRepository;
        this.bookCommentSqlRepository = bookCommentSqlRepository;
        this.userBookshelfSqlRepository = userBookshelfSqlRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.bookCommentSqlService = bookCommentSqlService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
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
        return bookCommentSqlService.getLatestComments(offset, limit);
    }

    public List<BookInfoDto> getLatestCollectBooksByCategory(String subCategory, int offset, int limit) {
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
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long totalCached = zSetOps.size(LIKED_BOOKS_CACHE_KEY);

            logger.info("嘗試從緩存中獲取最新點讚書籍。Offset: {}, Limit: {}, 緩存總數: {}", offset, limit, totalCached);

            if (totalCached < CACHE_SIZE || offset + limit > totalCached) {
                logger.info("緩存未命中或數據不足。從資料庫更新緩存。");
                long startTime = System.currentTimeMillis();
                updateFullCacheOfLatestLikes();
                logger.info("更新緩存耗時: {} ms", System.currentTimeMillis() - startTime);
            }

            long startTime = System.currentTimeMillis();
            Set<String> cachedBooks = zSetOps.reverseRange(LIKED_BOOKS_CACHE_KEY, offset, offset + limit - 1);
            logger.info("從緩存獲取數據耗時: {} ms", System.currentTimeMillis() - startTime);

            if (cachedBooks != null && cachedBooks.size() == limit) {
                logger.info("成功從緩存中檢索到 {} 本書", cachedBooks.size());
                startTime = System.currentTimeMillis();
                logger.info("反序列化耗時: {} ms", System.currentTimeMillis() - startTime);
                return deserializeBooks(new ArrayList<>(cachedBooks));
            } else {
                logger.warn("緩存檢索失敗或不完整。從資料庫獲取。");
                return getLatestLikedBooksFromDatabase(offset, limit);
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("連接 Redis 失敗。回退到資料庫。", e);
            return getLatestLikedBooksFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("獲取緩存中的最新點讚書籍時發生意外錯誤", e);
            return getLatestLikedBooksFromDatabase(offset, limit);
        }
    }

    private List<BookInfoDto> getLatestLikedBooksFromDatabase(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Long> bookIdPage = userBookshelfSqlRepository.findBookIdsByOrderByTimestampLikeDesc(pageable);
        List<Long> bookIds = bookIdPage.getContent();

        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, BookInfo> bookMap = bookInfoRepository.findByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getBookId, Function.identity()));

        List<BookInfo> sortedBooks = bookIds.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return BookInfoDtoConverter.convertToDtoList(sortedBooks);
    }

    private String serializeBook(BookInfo book) {
        try {
            return objectMapper.writeValueAsString(BookInfoDtoConverter.convertToDto(book));
        } catch (Exception e) {
            logger.error("序列化書籍時發生錯誤", e);
            throw new RuntimeException("序列化書籍失敗", e);
        }
    }

    private List<BookInfoDto> deserializeBooks(List<String> serializedBooks) {
        return serializedBooks.stream()
                .map(this::deserializeBook)
                .collect(Collectors.toList());
    }

    private BookInfoDto deserializeBook(String serialized) {
        try {
            return objectMapper.readValue(serialized, BookInfoDto.class);
        } catch (IOException e) {
            logger.error("反序列化書籍時發生錯誤", e);
            throw new RuntimeException("反序列化書籍失敗", e);
        }
    }

    private void updateFullCacheOfLatestLikes() {
        try {
            logger.info("使用最新點讚的書籍更新完整緩存");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            Page<UserBookshelfSql> latestLikes = userBookshelfSqlRepository.findByLikesTrueOrderByTimestampLikeDesc(PageRequest.of(0, CACHE_SIZE));
            logger.info("種共有幾本書" + latestLikes.getContent().size());

            redisTemplate.delete(LIKED_BOOKS_CACHE_KEY);
            logger.info("清除現有緩存");

            for (UserBookshelfSql like : latestLikes) {
                BookInfo book = like.getBookId();
                zSetOps.add(LIKED_BOOKS_CACHE_KEY, serializeBook(book), like.getTimestampLike().getTime());
            }
            logger.info("新增 {} 本書到緩存", latestLikes.getNumberOfElements());
        } catch (RedisConnectionFailureException e) {
            logger.error("由於 Redis 連接問題，無法更新完整緩存", e);
        } catch (Exception e) {
            logger.error("更新完整緩存時發生意外錯誤", e);
        }
    }

    public List<BookInfoDto> getLatestCollectBooks(int offset, int limit) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long totalCached = zSetOps.size(COLLECTED_BOOKS_CACHE_KEY);

            logger.info("嘗試從緩存中獲取最新收藏書籍。Offset: {}, Limit: {}, 緩存總數: {}", offset, limit, totalCached);

            if (totalCached < CACHE_SIZE || offset + limit > totalCached) {
                logger.info("緩存未命中或數據不足。從資料庫更新緩存。");
                long startTime = System.currentTimeMillis();
                updateFullCacheOfLatestCollects();
                logger.info("更新緩存耗時: {} ms", System.currentTimeMillis() - startTime);
            }

            long startTime = System.currentTimeMillis();
            Set<String> cachedBooks = zSetOps.reverseRange(COLLECTED_BOOKS_CACHE_KEY, offset, offset + limit - 1);
            logger.info("從緩存獲取數據耗時: {} ms", System.currentTimeMillis() - startTime);

            if (cachedBooks != null && cachedBooks.size() == limit) {
                logger.info("成功從緩存中檢索到 {} 本書", cachedBooks.size());
                startTime = System.currentTimeMillis();
                logger.info("反序列化耗時: {} ms", System.currentTimeMillis() - startTime);
                return deserializeBooks(new ArrayList<>(cachedBooks));
            } else {
                logger.warn("緩存檢索失敗或不完整。從資料庫獲取。");
                return getLatestCollectedBooksFromDatabase(offset, limit);
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("連接 Redis 失敗。回退到資料庫。", e);
            return getLatestCollectedBooksFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("獲取緩存中的最新點讚書籍時發生意外錯誤", e);
            return getLatestCollectedBooksFromDatabase(offset, limit);
        }
    }

    public List<BookInfoDto> getLatestCollectedBooksFromDatabase(int offset, int limit) {
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

    private void updateFullCacheOfLatestCollects() {
        try {
            logger.info("使用最新收藏的書籍更新完整緩存");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            Page<UserBookshelfSql> latestCollects = userBookshelfSqlRepository.findByCollectsTrueOrderByTimestampCollectDesc(PageRequest.of(0, CACHE_SIZE));
            logger.info("種共有幾本書" + latestCollects.getContent().size());

            redisTemplate.delete(COLLECTED_BOOKS_CACHE_KEY);
            logger.info("清除現有緩存");

            for (UserBookshelfSql collect : latestCollects) {
                BookInfo book = collect.getBookId();
                zSetOps.add(COLLECTED_BOOKS_CACHE_KEY, serializeBook(book), collect.getTimestampCollect().getTime());
            }
            logger.info("新增 {} 本書到緩存", latestCollects.getNumberOfElements());
        } catch (RedisConnectionFailureException e) {
            logger.error("由於 Redis 連接問題，無法更新完整緩存", e);
        } catch (Exception e) {
            logger.error("更新完整緩存時發生意外錯誤", e);
        }
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
