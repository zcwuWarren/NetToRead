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

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(name = "book_cover", nullable = false)
    private String bookCover;

    @Column(name = "publish_date", nullable = false)
    private String publishDate;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "collect")
    private Integer collect;

    @Column(name = "content")
    private String content;
}
