package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.Quote;
import com.personal_project.Next_to_read.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteDto {
    private Long userId;
    private String quote;
    private String bookName;
    private String date;
    private Long bookId;
    private String userName;
    private Long id;

    public QuoteDto(Quote quote) {
        this.userId = quote.getUserId().getUserId();
        this.quote = quote.getQuote();
        this.bookName = quote.getBookId().getBookName();
        this.date = DateUtil.formatDate(quote.getTimestamp());
        this.bookId = quote.getBookId().getBookId();
        this.userName = quote.getUserId().getName();
        this.id = quote.getId();
    }
}

