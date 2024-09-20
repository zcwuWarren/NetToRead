package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.Quote;
import lombok.Data;

@Data
public class QuoteDto {
    private Long userId;
    private String quoteText;

    public QuoteDto(Quote quote) {
        this.userId = quote.getUser().getUserId();
        this.quoteText = quote.getQuoteText();
    }
}

