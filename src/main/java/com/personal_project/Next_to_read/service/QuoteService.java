package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private QuoteRepository quoteRepository;
    private JwtTokenUtil jwtTokenUtil;

    public QuoteService(QuoteRepository quoteRepository, JwtTokenUtil jwtTokenUtil) {
        this.quoteRepository = quoteRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<QuoteDto> getQuotesByBookId(Long bookId) {

        List<Quote> quotes = quoteRepository.findByBookId_BookIdOrderByTimestampDesc(bookId);
        return quotes.stream().map(quote -> new QuoteDto(quote)).collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesByUserId(String token) {

        User user = jwtTokenUtil.getUserFromToken(token);
        Long userId = user.getUserId();

        List<Quote> quotes = quoteRepository.findByUserId_UserIdOrderByTimestampDesc(userId);
        return quotes.stream().map(quote -> new QuoteDto(quote)).collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesBySubCategory(String subCategory) {

        List<Quote> quotes = quoteRepository.findBySubCategory_OrderByTimestampDesc(subCategory);
        return quotes.stream().map(quote -> new QuoteDto(quote)).collect(Collectors.toList());
    }

    public List<QuoteDto> getQuotesWithoutCondition() {
        List<Quote> quotes = quoteRepository.findTop6ByOrderByTimestampDesc();
        return quotes.stream().map(quote -> new QuoteDto(quote)).collect(Collectors.toList());
    }
}

