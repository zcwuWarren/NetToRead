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

            logger.info("get latest liked book from cache. Offset: {}, Limit: {}, total cached size: {}", offset, limit, totalCached);

            if (totalCached < CACHE_SIZE || offset + limit > totalCached) {
                logger.info("cache miss or insufficient data, updating cache from database");
                updateFullCacheOfLatestLikes();
            }

            // get liked book from cache
            long startTime = System.currentTimeMillis();
            Set<String> cachedBooks = zSetOps.reverseRange(LIKED_BOOKS_CACHE_KEY, offset, offset + limit - 1);

            if (cachedBooks != null && cachedBooks.size() == limit) {
                logger.info("get {} books from cache", cachedBooks.size());
                return deserializeBooks(new ArrayList<>(cachedBooks));
            } else {
                logger.warn("get cache failed, get data form database");
                return getLatestLikedBooksFromDatabase(offset, limit);
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("connect redis failed, get data from database", e);
            return getLatestLikedBooksFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("error in getting latest liked from cache", e);
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
            logger.error("error in serializing book error", e);
            throw new RuntimeException("serialized failed", e);
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
            logger.error("error in deserializing book error", e);
            throw new RuntimeException("deserialized failed", e);
        }
    }

    private void updateFullCacheOfLatestLikes() {
        try {
            logger.info("update latest liked book to fully update cache");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            Page<UserBookshelfSql> latestLikes = userBookshelfSqlRepository.findByLikesTrueOrderByTimestampLikeDesc(PageRequest.of(0, CACHE_SIZE));

            redisTemplate.delete(LIKED_BOOKS_CACHE_KEY);
            logger.info("delete current cache");

            for (UserBookshelfSql like : latestLikes) {
                BookInfo book = like.getBookId();
                zSetOps.add(LIKED_BOOKS_CACHE_KEY, serializeBook(book), like.getTimestampLike().getTime());
            }
            logger.info("add {} books to cache", latestLikes.getNumberOfElements());
        } catch (RedisConnectionFailureException e) {
            logger.error("redis connect error, update full cache of latest like failed", e);
        } catch (Exception e) {
            logger.error("error in fully updated cache", e);
        }
    }

    public List<BookInfoDto> getLatestCollectBooks(int offset, int limit) {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long totalCached = zSetOps.size(COLLECTED_BOOKS_CACHE_KEY);

            logger.info("get latest collected book from cache. Offset: {}, Limit: {}, total cached size: {}", offset, limit, totalCached);

            if (totalCached < CACHE_SIZE || offset + limit > totalCached) {
                logger.info("cache miss or insufficient data, updating cache from database");
                updateFullCacheOfLatestCollects();
            }

            Set<String> cachedBooks = zSetOps.reverseRange(COLLECTED_BOOKS_CACHE_KEY, offset, offset + limit - 1);

            if (cachedBooks != null && cachedBooks.size() == limit) {
                logger.info("get {} books from cache", cachedBooks.size());
                return deserializeBooks(new ArrayList<>(cachedBooks));
            } else {
                logger.warn("get cache failed, get data form database");
                return getLatestCollectedBooksFromDatabase(offset, limit);
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("connect redis failed, get data from database", e);
            return getLatestCollectedBooksFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("error in getting latest liked from cache", e);
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
            logger.info("update latest collected book to fully update cache");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            Page<UserBookshelfSql> latestCollects = userBookshelfSqlRepository.findByCollectsTrueOrderByTimestampCollectDesc(PageRequest.of(0, CACHE_SIZE));

            redisTemplate.delete(COLLECTED_BOOKS_CACHE_KEY);
            logger.info("delete current cache");

            for (UserBookshelfSql collect : latestCollects) {
                BookInfo book = collect.getBookId();
                zSetOps.add(COLLECTED_BOOKS_CACHE_KEY, serializeBook(book), collect.getTimestampCollect().getTime());
            }
            logger.info("add {} books to cache", latestCollects.getNumberOfElements());
        } catch (RedisConnectionFailureException e) {
            logger.error("redis connect error, update full cache of latest like failed", e);
        } catch (Exception e) {
            logger.error("error in fully updated cache", e);
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
