package com.personal_project.Next_to_read.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.data.form.QuoteForm;
import com.personal_project.Next_to_read.exception.ResourceNotFoundException;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.BookInfo;
import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.BookInfoRepository;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import com.personal_project.Next_to_read.util.DateUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private QuoteRepository quoteRepository;
    private JwtTokenUtil jwtTokenUtil;
    private final BookInfoRepository bookInfoRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private static final String CACHE_KEY = "quotes_sorted_set";
    private static final int CACHE_SIZE = 50;

    public QuoteService(QuoteRepository quoteRepository, JwtTokenUtil jwtTokenUtil, BookInfoRepository bookInfoRepository, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.quoteRepository = quoteRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.bookInfoRepository = bookInfoRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<QuoteDto> getQuotesByBookId(Long bookId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Quote> quotesPage = quoteRepository.findByBookIdOrderByTimestampDesc(bookId, pageable);
        return quotesPage.getContent().stream()
                .map(QuoteDto::new)
                .collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesByUserId(String token, int offset, int limit) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Quote> quotesPage = quoteRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return quotesPage.getContent().stream()
                .map(QuoteDto::new)
                .collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesBySubCategory(String subCategory, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Quote> quotesPage = quoteRepository.findBySubCategoryOrderByTimestampDesc(subCategory, pageable);
        return quotesPage.getContent().stream()
                .map(QuoteDto::new)
                .collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesWithoutCondition(int offset, int limit) {
        assert limit == CACHE_SIZE : "Limit must be equal to CACHE_SIZE";

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long totalCached = zSetOps.size(CACHE_KEY);

            logger.info("Attempting to fetch quotes. Offset: {}, Limit: {}, Total cached: {}", offset, limit, totalCached);

            // 如果是請求第一頁（offset == 0），且緩存不滿，則更新緩存
            if (offset == 0 && totalCached < CACHE_SIZE) {
                logger.info("Requesting first page and cache is not full. Updating cache from database.");
                updateFullCache();
            }

            if (offset == 0) {
                Set<String> cachedQuotes = zSetOps.reverseRange(CACHE_KEY, 0, CACHE_SIZE - 1);
                if (cachedQuotes != null && cachedQuotes.size() == CACHE_SIZE) {
                    logger.info("Retrieved first page ({} quotes) from cache", cachedQuotes.size());
                    return deserializeQuotes(new ArrayList<>(cachedQuotes));
                }
            }

            logger.info("Fetching quotes from database. Offset: {}, Limit: {}", offset, limit);
            return getQuotesFromDatabase(offset, limit);
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to connect to Redis. Falling back to database.", e);
            return getQuotesFromDatabase(offset, limit);
        } catch (Exception e) {
            logger.error("Unexpected error when fetching quotes", e);
            return getQuotesFromDatabase(offset, limit);
        }
    }

    private List<QuoteDto> getQuotesFromDatabase(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("timestamp").descending());
        Page<Quote> quotesPage = quoteRepository.findAll(pageable);
        return quotesPage.getContent().stream()
                .map(QuoteDto::new)
                .collect(Collectors.toList());
    }

    private void updateFullCache() {
        try {
            logger.info("Updating full cache with latest quotes");
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            List<Quote> latestQuotes = quoteRepository.findAll(
                    PageRequest.of(0, CACHE_SIZE, Sort.by("timestamp").descending())
            ).getContent();

            redisTemplate.delete(CACHE_KEY);
            logger.info("Cleared existing cache");

            for (Quote quote : latestQuotes) {
                zSetOps.add(CACHE_KEY, serializeQuote(quote), quote.getTimestamp().getTime());
            }
            logger.info("Added {} quotes to cache", latestQuotes.size());
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to update full cache due to Redis connection issue", e);
        } catch (Exception e) {
            logger.error("Unexpected error when updating full cache", e);
        }
    }

    @Transactional
    public void addQuote(Long bookId, String token, QuoteForm quoteForm) {
        try {
            logger.info("Adding new quote for book ID: {}", bookId);

            // get user form token
            User user = jwtTokenUtil.getUserFromToken(token);

            // find BookInfo
            BookInfo bookInfo = bookInfoRepository.findByBookId(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

            // set quote
            Quote quote = new Quote();
            quote.setBookId(bookInfo);
            quote.setUserId(user);
            quote.setQuote(quoteForm.getQuote());
            quote.setTimestamp(Timestamp.from(Instant.now()));
            quote.setMainCategory(bookInfo.getMainCategory());
            quote.setSubCategory(bookInfo.getSubCategory());

            quoteRepository.save(quote);
            logger.info("Quote saved to database with ID: {}", quote.getId());

            // 更新緩存
            updateCache(quote);
        } catch (Exception e) {
            logger.error("Error adding new quote", e);
            throw new RuntimeException("Failed to add new quote", e);
        }
    }

    private void updateCache(Quote newQuote) {
        try {
            logger.info("Updating cache with new quote"); // OK
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            long cacheSize = zSetOps.size(CACHE_KEY);

            if (cacheSize < CACHE_SIZE) {
                zSetOps.add(CACHE_KEY, serializeQuote(newQuote), newQuote.getTimestamp().getTime());
                logger.info("Added new quote to cache. Current cache size: {}", cacheSize + 1);
            } else {
                Double oldestScore = zSetOps.score(CACHE_KEY, zSetOps.range(CACHE_KEY, 0, 0).iterator().next());
                if (newQuote.getTimestamp().getTime() > oldestScore) {
                    zSetOps.removeRange(CACHE_KEY, 0, 0);
                    zSetOps.add(CACHE_KEY, serializeQuote(newQuote), newQuote.getTimestamp().getTime());
                    logger.info("Replaced oldest quote in cache with new quote"); // OK
                } else {
                    logger.info("New quote is older than cached quotes, not added to cache");
                }
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to update cache due to Redis connection issue", e);
        } catch (Exception e) {
            logger.error("Unexpected error when updating cache", e);
        }
    }

    private String serializeQuote(Quote quote) {
        try {
            ObjectNode node = objectMapper.createObjectNode()
                    .put("id", quote.getId())
                    .put("quote", quote.getQuote())
                    .put("timestamp", quote.getTimestamp().getTime())
                    .put("mainCategory", quote.getMainCategory())
                    .put("subCategory", quote.getSubCategory())
                    .put("userId", quote.getUserId().getUserId())
                    .put("bookId", quote.getBookId().getBookId())
                    .put("userName", quote.getUserId().getName())
                    .put("bookName", quote.getBookId().getBookName());

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing quote", e);
        }
    }

    private List<QuoteDto> deserializeQuotes(List<String> serializedQuotes) {
        return serializedQuotes.stream()
                .map(this::deserializeQuote)
                .map(this::convertToQuoteDto)
                .collect(Collectors.toList());
    }

    private Quote deserializeQuote(String serialized) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(serialized);

            Quote quote = new Quote();
            quote.setId(node.get("id").asLong());
            quote.setQuote(node.get("quote").asText());
            quote.setTimestamp(new Timestamp(node.get("timestamp").asLong()));
            quote.setMainCategory(node.get("mainCategory").asText());
            quote.setSubCategory(node.get("subCategory").asText());

            User user = new User();
            user.setUserId(node.get("userId").asLong());
            user.setName(node.get("userName").asText());
            quote.setUserId(user);

            BookInfo bookInfo = new BookInfo();
            bookInfo.setBookId(node.get("bookId").asLong());
            bookInfo.setBookName(node.get("bookName").asText());
            quote.setBookId(bookInfo);

            return quote;
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing quote", e);
        }
    }

    private QuoteDto convertToQuoteDto(Quote quote) {
        return new QuoteDto(
                quote.getUserId().getUserId(),
                quote.getQuote(),
                quote.getBookId().getBookName(),
                DateUtil.formatDate(quote.getTimestamp()),
                quote.getBookId().getBookId(),
                quote.getUserId().getName(),
                quote.getId()
        );
    }

    public boolean deleteQuote(Long id, String token) {
        try {
            User user = jwtTokenUtil.getUserFromToken(token);
            Optional<Quote> quoteOpt = quoteRepository.findById(id);
            if (quoteOpt.isPresent()) {
                Quote quote = quoteOpt.get();
                if (quote.getUserId().getUserId().equals(user.getUserId())) {
                    quoteRepository.delete(quote);

                    // delete from cache
                    ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
                    Set<String> cachedQuotes = zSetOps.rangeByScore(CACHE_KEY, quote.getTimestamp().getTime(), quote.getTimestamp().getTime());
                    Long removed = 0L;
                    if (!cachedQuotes.isEmpty()) {
                        removed = zSetOps.remove(CACHE_KEY, cachedQuotes.iterator().next());
                    }
                    logger.info("Removed {} entries from cache", removed);

                    // if deleted quote in cache, getting new quote into cache
                    if (removed > 0) {
                        updateCacheAfterDeletion();
                    }

                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting quote", e);
            return false;
        }
    }

    private void updateCacheAfterDeletion() {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        long cacheSize = zSetOps.size(CACHE_KEY);
        if (cacheSize < CACHE_SIZE) {
            // get timestamp of oldest quote in cache
            Set<String> oldestQuoteSet = zSetOps.range(CACHE_KEY, -1, -1);
            if (oldestQuoteSet.isEmpty()) {
                logger.warn("Cache is empty after deletion. Unable to determine oldest quote.");
                return;
            }
            String oldestQuoteStr = oldestQuoteSet.iterator().next();
            Quote oldestQuote = deserializeQuote(oldestQuoteStr);
            Timestamp oldestTimestamp = oldestQuote.getTimestamp();

            // get next older quote by timestamp from database into cache
            List<Quote> nextQuotes = quoteRepository.findFirstByTimestampLessThanOrderByTimestampDesc(
                    oldestTimestamp,
                    PageRequest.of(0, 1)
            );

            if (!nextQuotes.isEmpty()) {
                Quote nextQuote = nextQuotes.get(0);
                String serializedNextQuote = serializeQuote(nextQuote);
                zSetOps.add(CACHE_KEY, serializedNextQuote, nextQuote.getTimestamp().getTime());
                logger.info("Added next quote to cache after deletion. Cache size: {}", cacheSize + 1);
            } else {
                logger.info("No more quotes available to add to cache after deletion.");
            }
        }
    }

    public boolean editQuote(Long id, String token, String updatedQuoteContent) {
        try {
            User user = jwtTokenUtil.getUserFromToken(token);
            Optional<Quote> quoteOpt = quoteRepository.findById(id);

            if (quoteOpt.isPresent() && quoteOpt.get().getUserId().getUserId().equals(user.getUserId())) {
                Quote quoteToUpdate = quoteOpt.get();
                ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

                // find quote in cache
                Set<String> cachedQuotes = zSetOps.rangeByScore(CACHE_KEY, quoteToUpdate.getTimestamp().getTime(), quoteToUpdate.getTimestamp().getTime());
                boolean inCache = !cachedQuotes.isEmpty();

                // update Quote content without updating timestamp
                quoteToUpdate.setQuote(updatedQuoteContent);
                quoteRepository.save(quoteToUpdate);

                if (inCache) {
                    // if quote in cache, updating ，updating cache
                    String oldSerializedQuote = cachedQuotes.iterator().next();
                    zSetOps.remove(CACHE_KEY, oldSerializedQuote);
                    String newSerializedQuote = serializeQuote(quoteToUpdate);
                    zSetOps.add(CACHE_KEY, newSerializedQuote, quoteToUpdate.getTimestamp().getTime());
                    logger.info("Updated quote in cache. ID: {}", id);
                } else {
                    logger.info("Quote not in cache, no cache update needed. ID: {}", id);
                }

                logger.info("Quote updated successfully in database. ID: {}", id);
                return true;
            } else {
                logger.warn("Failed to edit quote. ID: {}. Quote not found or unauthorized.", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error editing quote with ID: {}", id, e);
            return false;
        }
    }
}

