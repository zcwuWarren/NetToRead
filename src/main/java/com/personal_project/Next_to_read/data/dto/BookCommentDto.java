package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCommentDto {
    private Long userId;
    private String comment;
    private String bookName;
    private String date;

    public BookCommentDto(BookCommentSql comment) {
        this.userId = comment.getUserId().getUserId();
        this.comment = comment.getComment();
        this.bookName = comment.getBookId().getBookName();
        this.date = DateUtil.formatDate(comment.getTimestamp());
    }
}
