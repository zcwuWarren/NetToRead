package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.Quote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteDto {
    private Long userId;
    private String quote;

    public QuoteDto(Quote quote) {
        this.userId = quote.getUserId().getUserId();
        this.quote = quote.getQuote();
    }
}

