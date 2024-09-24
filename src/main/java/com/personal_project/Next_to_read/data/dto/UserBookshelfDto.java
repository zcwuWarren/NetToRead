package com.personal_project.Next_to_read.data.dto;

import com.personal_project.Next_to_read.model.UserBookshelfSql;
import com.personal_project.Next_to_read.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookshelfDto {
    // todo add bookId
    private Long userId;
    private String bookName;
    private String bookCover;
    private String dateOfLike;
    private String dateOfCollect;

    public UserBookshelfDto(UserBookshelfSql userBookshelfSql) {
        this.userId = userBookshelfSql.getUserId().getUserId();
        this.bookName = userBookshelfSql.getBookId().getBookName();
        this.bookCover = userBookshelfSql.getBookId().getBookCover();
        this.dateOfLike = DateUtil.formatDate(userBookshelfSql.getTimestampLike());
        this.dateOfCollect = DateUtil.formatDate(userBookshelfSql.getTimestampCollect());
    }
}
