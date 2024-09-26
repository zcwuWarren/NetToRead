package com.personal_project.Next_to_read.util.EntityToDtoConverter;

import com.personal_project.Next_to_read.data.dto.BookCommentDto;
import com.personal_project.Next_to_read.model.BookCommentSql;
import com.personal_project.Next_to_read.util.DateUtil;

import java.util.List;
import java.util.stream.Collectors;

public class BookCommentDtoConverter {
    public static BookCommentDto convertToDto(BookCommentSql comment) {
        return BookCommentDto.builder()
                .userId(comment.getUserId().getUserId())
                .comment(comment.getComment())
                .bookName(comment.getBookId().getBookName())
                .bookId(comment.getBookId().getBookId())
                .date(DateUtil.formatDate(comment.getTimestamp()))
                .build();
    }

    public static List<BookCommentDto> convertToDtoList(List<BookCommentSql> comments) {
        return comments.stream()
                .map(BookCommentDtoConverter::convertToDto)
                .collect(Collectors.toList());
    }
}
