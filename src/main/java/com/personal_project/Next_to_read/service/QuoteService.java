package com.personal_project.Next_to_read.service;

import com.personal_project.Next_to_read.data.dto.QuoteDto;
import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuoteService {

    private QuoteRepository quoteRepository;

    public QuoteService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public List<QuoteDto> getQuotesByBookId(Long bookId) {

        List<Quote> quotes = quoteRepository.findByBook_BookId(bookId);
        return quotes.stream().map(quote -> new QuoteDto(quote)).collect(Collectors.toList());
    }
}

