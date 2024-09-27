package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.jwt.JwtTokenUtil;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.model.User;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public boolean deleteQuote(Long id, String token) {
        User user = jwtTokenUtil.getUserFromToken(token);

        // 查找該評論
        Optional<Quote> quoteOpt = quoteRepository.findById(id);
        if (quoteOpt.isPresent()) {
            Quote quote = quoteOpt.get();

            // 驗證該評論是否屬於當前用戶
            if (quote.getUserId().getUserId().equals(user.getUserId())) {
                quoteRepository.delete(quote);
                return true;  // 刪除成功
            }
        }
        return false;  // 刪除失敗，因為評論不存在或者權限不足
    }

    public boolean editQuote(Long id, String token, String updatedQuote) {
        User user = jwtTokenUtil.getUserFromToken(token);
        Optional<Quote> quote = quoteRepository.findById(id);

        if (quote.isPresent() && quote.get().getUserId().getUserId().equals(user.getUserId())) {
            Quote quoteToUpdate = quote.get();
            quoteToUpdate.setQuote(updatedQuote); // 更新引言內容
            quoteRepository.save(quoteToUpdate);
            return true;
        }
        return false;
    }
}

