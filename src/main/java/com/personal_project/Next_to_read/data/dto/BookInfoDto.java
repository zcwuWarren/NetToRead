package com.personal_project.Next_to_read.data.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.awt.print.Book;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookInfoDto {
    private Long bookId;
    private Long isbn;
    private String mainCategory;
    private String subCategory;
    private String bookName;
    private String bookCover;
    private String publishDate;
    private String publisher;
    private String author;
    private Integer like;
    private Integer collect;
    private String content;
    private String description;
}
