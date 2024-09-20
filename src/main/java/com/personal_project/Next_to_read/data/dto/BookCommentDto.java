package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.BookCommentSql;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCommentDto {
    private Long userId;
    private String comment;

    public BookCommentDto(BookCommentSql comment) {
        this.userId = comment.getUserId().getUserId();
        this.comment = comment.getComment();
    }
}
