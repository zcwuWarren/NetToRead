package com.personal_project.Next_to_read.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoDto {
    private Long bookId;
    private Long isbn;
    private String category;
    private String bookName;
    private String bookCover;
    private String publishDate;
    private String publisher;
    private Integer like;
    private Integer collect;
    private String content;
}
