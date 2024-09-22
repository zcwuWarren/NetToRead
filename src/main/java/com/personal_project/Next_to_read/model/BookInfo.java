package com.personal_project.Next_to_read.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="book_info")
@Data
public class BookInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="book_id")
    private Long bookId;

    @Column(name = "ISBN", nullable = false)
    private Long isbn;

    @Column(name = "main_category", nullable = false)
    private String mainCategory;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(name = "book_cover", nullable = false)
    private String bookCover;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "publish_date", nullable = false)
    private String publishDate;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "collect")
    private Integer collect;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "description", nullable = false)
    private String description;
}
